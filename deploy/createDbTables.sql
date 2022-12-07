--One DEID_RUN entry is generated for each note. It contains when that note was processed, which version of CliniDeID was used, and which options were selected
--Level_of_Deid is currently one of 'Beyond Safe Harbor' 'HIPAA Safe Harbor' 'HIPAA Limited'
--Options details the user chosen options for this run, including input source
CREATE TABLE DEID_RUN (Run_ID bigserial primary key, note_id bigint, System_Used varchar(200), Level_Of_Deid varchar(500),  Date_Time_Processed timestamp, Options varchar(500));

--NOTE_DEID entries correspond to output documents from CliniDeID, they contain a foreign key run_id to DEID_RUN, the resulting text, and what was done with PII
--PII_TREATMENT options are Resynthesized, TAG (Category), TAG (PII) corresponding to outputTypes 'resynthesis', 'categorytag', and 'generaltag'
--multiple entries could result from 1 document if multiple outputTypes have been chosen
CREATE TABLE NOTE_DEID (NoteDeid_ID bigserial primary key, run_id bigint, Text_Deid text,  PII_Treatment varchar(25));

--NOTE_ANNOTATIONS contain an entry for every PII found in a document. Result of outputType 'detectedPII'.
CREATE TABLE NOTE_ANNOTATIONS ( Note_Annotation_ID bigserial primary key, run_id bigint, span_begin int, span_end int, category varchar(50));
