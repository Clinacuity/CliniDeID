
/*
# © Copyright 2019-2023, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

package com.clinacuity.deid.readers;

import com.clinacuity.clinideid.message.DeidLevel;
import com.clinacuity.deid.mains.DeidPipeline;
import com.clinacuity.deid.type.DocumentInformationAnnotation;
import com.clinacuity.deid.util.ConnectionProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class DbCollectionReader extends GeneralCollectionReader {
    public static final String SERVER = "server";
    public static final String DBMS = "dbms";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SCHEMA = "schema";
    public static final String PORT = "port";
    public static final String DB_NAME = "dbName";
    public static final String COLUMN_ID = "columnId";
    public static final String COLUMN_TEXT = "columnText";
    public static final String TABLE_NAME = "tableName";
    public static final String QUERY = "whereClause";
    public static final String DB_OUTPUT_SELECT = "dbOutputSelected";
    public static final String OPTION_STRING = "optionString";
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final Map<DeidLevel, String> PRETTY_LEVEL_STRING = Map.of(DeidLevel.beyond, "Beyond Safe Harbor", DeidLevel.strict, "HIPAA Safe Harbor", DeidLevel.limited, "HIPPA Limited data set");

    @ConfigurationParameter(name = SERVER, mandatory = false, defaultValue = "")
    private String server = null;
    @ConfigurationParameter(name = DBMS)
    private String dbms = null;
    @ConfigurationParameter(name = USERNAME, mandatory = false, defaultValue = "")
    private String username = null;
    @ConfigurationParameter(name = PASSWORD, mandatory = false, defaultValue = "")
    private String password = null;
    @ConfigurationParameter(name = SCHEMA, mandatory = false, defaultValue = "")
    private String schema = null;
    @ConfigurationParameter(name = PORT, mandatory = false, defaultValue = "")
    private String port = null;
    @ConfigurationParameter(name = DB_NAME)
    private String dbName = null;
    @ConfigurationParameter(name = COLUMN_ID, mandatory = false, defaultValue = "")
    private String columnId = null;
    @ConfigurationParameter(name = COLUMN_TEXT, mandatory = false, defaultValue = "")
    private String columnText = null;
    @ConfigurationParameter(name = TABLE_NAME, mandatory = false, defaultValue = "")
    private String tableName = null;
    @ConfigurationParameter(name = QUERY, mandatory = false, defaultValue = "")
    private String whereClause = null;
    @ConfigurationParameter(name = DB_OUTPUT_SELECT, mandatory = false, defaultValue = "")
    private boolean dbOutputSelected = false;
    @ConfigurationParameter(name = OPTION_STRING, mandatory = false, defaultValue = "")
    private String optionString;
    private int count = 0;
    private Connection inputDbConnection = null;
    private Connection outputDbConnection = null;
    private PreparedStatement getDataStatement = null;
    private int currentIndex = 0;
    private ResultSet resultSet = null;
    private int counter = 0;
    private String lastProcessed;
    private String currentProcessed;

    /**
     * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        if (dbms.isEmpty() || dbName.isEmpty() || columnText.isEmpty() || columnId.isEmpty() || tableName.isEmpty()) {
            throw new ResourceInitializationException("Missing parameter values: dbms, name, columnId, columnText, tableName are required", null);
        }

        try {//This doesn't use singleton as it will be only the connection to input DB, Singleton usage is for output db
            //also, different settings for auto commit which would affect output inserts

            inputDbConnection = ConnectionProperties.makeConnection(dbms, server, port, dbName, username, password, schema);
            inputDbConnection.setAutoCommit(false);
            PreparedStatement getCountStatement;
            //get count of notes
            if (!whereClause.isEmpty()) {
                getCountStatement = inputDbConnection.prepareStatement("SELECT COUNT (*) FROM ? ?");
                getCountStatement.setString(2, whereClause);
                getDataStatement = inputDbConnection.prepareStatement("SELECT ?, ? FROM ? ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                getDataStatement.setString(4, whereClause);
            } else {
                getCountStatement = inputDbConnection.prepareStatement("SELECT COUNT (*) FROM ?");
                getDataStatement = inputDbConnection.prepareStatement("SELECT ?, ? FROM ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            }
            getCountStatement.setString(1, tableName);
            resultSet = getCountStatement.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
            resultSet.close();
            getCountStatement.close();

            //get notes
            getDataStatement.setString(1, columnId);
            getDataStatement.setString(2, columnText);
            getDataStatement.setString(3, tableName);
            getDataStatement.setFetchSize(50);
            resultSet = getDataStatement.executeQuery();
            getDataStatement.closeOnCompletion();
            LOGGER.debug("DB Input of {} records", count);
            if (dbOutputSelected) {
                outputDbConnection = ConnectionProperties.getInstance().getConnection();
            }
        } catch (NullPointerException | SQLException e) {
            LOGGER.throwing(e);
            try {
                inputDbConnection.close();
            } catch (SQLException e1) {
                LOGGER.throwing(e1);
            }
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public int getFileCount() {
        return count;
    }

    @Override
    public void setMaximumProcessableFiles(int maximumProcessableFiles) {
        if (fileLimit > maximumProcessableFiles) {
            fileLimit = maximumProcessableFiles;
        }
    }

    @Override
    public void getNext(JCas jCas) throws IOException {
        lastProcessed = currentProcessed;
        currentIndex++;
        String note;
        String noteId;

        do {
            try {
                noteId = resultSet.getString(1);
                note = resultSet.getString(2);
            } catch (SQLException e) {
                LOGGER.throwing(e);
                throw new IOException(e);
            }
            if (note.length() > fileSizeLimit) {
                LOGGER.debug("Note {} was too big at {} characters. It was skipped.", noteId, note.length());
            }
        } while (note.length() > fileSizeLimit && hasNext());
        DocumentInformationAnnotation documentInformation = makeSourceDocumentInformation(jCas, noteId, "", note.length());
        if (server != null && !server.isEmpty()) {
            documentInformation.setFilePath(server + ":" + noteId);
            currentProcessed = server + ":" + noteId;
        } else {
            documentInformation.setFilePath(noteId);
            currentProcessed = noteId;
        }
        if (inputCda) {
            documentInformation.setOriginalXml(note);
            note = CdaXmlToText.process(jCas, note);
        }
        jCas.setDocumentText(note);
        jCas.setDocumentLanguage("en-us");
        makePiiOptionMapAnnotation(jCas);

        //if there is an output database, then insert row into DEID_RUN with details of this run
        if (dbOutputSelected) {
            long run_id = -1;
            ResultSet generatedKey = null;
            try (PreparedStatement insertStatement = outputDbConnection.prepareStatement("INSERT INTO DEID_RUN (note_id, date_time_processed, system_used, level_of_deid, options) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING run_id")) {
                insertStatement.setString(1, noteId);
                insertStatement.setString(2, LocalDateTime.now().toString());
                insertStatement.setString(3, DeidPipeline.VERSION);
                insertStatement.setString(4, PRETTY_LEVEL_STRING.get(deidLevel));
                insertStatement.setString(5, optionString);

                if (insertStatement.executeUpdate() > 0) {
                    generatedKey = insertStatement.getGeneratedKeys();
                    if (generatedKey != null && generatedKey.next()) {
                        run_id = generatedKey.getLong(1);
                    } else {
                        run_id = counter++;
                    }
                }
                LOGGER.debug("DEID_RUN's generated run_id for {} is {}", noteId, run_id);
            } catch (SQLException | NullPointerException e) {
                LOGGER.throwing(e);
                throw new IOException(e);
            } finally {
                if (generatedKey != null) {
                    try {
                        generatedKey.close();
                    } catch (SQLException e2) {//should e2 be thrown?
                        // If there is an Exception e, then it will be lost if e2 is thrown
                        // if there is no e, then it is OK to continue as generatedKey is local variable
                        LOGGER.throwing(e2);
                    }
                }
            }
            documentInformation.setRunId(run_id);
        }
    }

    @Override
    public boolean hasNext() {
        if (currentIndex >= fileLimit) {
            return false;
        }
        try {
            return (resultSet.next());//issue if hasNext() is called multiple times then documents will get skipped
        } catch (SQLException e) {
            LOGGER.throwing(e);
        }
        return false;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[]{
                new ProgressImpl(currentIndex, count, Progress.ENTITIES)
        };
    }

    @Override
    public void close() throws IOException {
        SQLException error = null;
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            error = e;
        }
        try {
            if (getDataStatement != null) {//not sure if these could be moved to initialize since resultSet is already made
                getDataStatement.close();
            }
        } catch (SQLException e) {
            if (error != null) {
                error.setNextException(e);
            }
            error = e;
        }
        try {
            if (inputDbConnection != null) {
                inputDbConnection.close();
            }
        } catch (SQLException e) {
            if (error != null) {
                error.setNextException(e);
            }
            error = e;
        }
        try {
            if (outputDbConnection != null) {
                outputDbConnection.close();
            }
        } catch (SQLException e) {
            if (error != null) {
                error.setNextException(e);
            }
            error = e;
        }
        if (error != null) {
            LOGGER.throwing(error);
            throw new IOException(error);
        }
    }

    @Override
    public String getLastFilenameProcessed() {
        if (currentIndex < 1) {
            return "";
        }
        return lastProcessed;
    }
}