<!--
This file is part of CliniDeID.
CliniDeID is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with CliniDeID. If not, see <https://www.gnu.org/licenses/>.
-->

# CliniDeID
## _Automatic clinical data de-identification_


[![GitHub license](https://img.shields.io/badge/license-GPL--3.0-blue)](https://www.gnu.org/licenses/gpl-3.0.txt)

CliniDeID automatically de-identifies clinical notes and structured data according to the [HIPAA Safe Harbor method](https://www.hhs.gov/hipaa/for-professionals/privacy/special-topics/de-identification/index.html#standard). It accurately finds identifiers and tags or replaces them with realistic surrogates for better anonymity. It improves access to richer, more detailed, and more accurate clinical data for clinical researchers. It eases research data sharing, and helps healthcare organizations protect patient data confidentiality.
### Features
* Clinical text de-identification: Uses advanced artificial intelligence algorithms to accurately identify all mentions of identifiers (PII) in unstructured clinical text notes and replaces them with realistic surrogates (PII resynthesis) or tags, as desired. Does not rely on any known identifiers but can use known identifiers to double-check the PII identification if available. Generalizes well to all common types of clinical notes.
* Structured data de-identification, integrated with unstructured text de-identification for consistent de-identification throughout the patient record (in CliniDeiD-Complete). Currently compatible with standard data models: OMOP CDM v5.3 and v6; HL7 FHIR coming soon)
* Replacement of identified PII with realistic surrogates and consistently across the whole patient record (PII resynthesis), or with tags (generic or PII categories).
* Highly accurate identification of PII (as demonstrated in several peer-reviewed evaluations and comparisons available at the bottom of this page)
* Multiple input and output data formats: plain text, HL7 CDA , relational databases (PostgreSQL, Oracle , MySQL, MS SQL Server, DB2)

This version of CliniDeID was built to be used on-premises and includes a user-friendly graphical user interface and can also be run from the command line (details below).


## Overview

CliniDeID includes different packages:

* `pachage1` : The first package.
* `package2` : A set of [link](https://uima.apache.org) annotators.

## Getting Started

Text...:

- Type some Markdown on the left
- See HTML in the right
- ✨Magic ✨

Markdown is a lightweight markup language based on the formatting conventions
that people naturally use in email.
As [John Gruber] writes on the [Markdown site][df1]

> The overriding design goal for Markdown's
> formatting syntax is to make it as readable
> as possible. The idea is that a
> Markdown-formatted document should be
> publishable as-is, as plain text, without
> looking like it's been marked up with tags
> or formatting instructions.


## Installation

CliniDeID requires [Java etc.](https://nodejs.org/) vX to run.

Install ....

```sh
cd clinideid
npm i
node app
```

For production environments...

```sh
npm install --production
NODE_ENV=production node app
```

## Plugins

CliniDeID is currently extended with the following plugins.
Instructions on how to use them in your own application are linked below.

| Plugin | README |
| ------ | ------ |
| Dropbox | [plugins/dropbox/README.md](https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md) |



#### Building for source

For production release:

```sh
gulp build --prod
```

Generating pre-built zip archives for distribution:

```sh
gulp build dist --prod
```

## Funding
CliniDeID's development was funded in part by the U.S. NIGMS (R41GM116479 and R42GM116479). We thank Youngjun Kim and all software developers and engineers at Clinacuity and the Medical University of South Carolina who made the application development and release possible.

## License

GPL version 3.0
![GPL](https://www.gnu.org/graphics/gplv3-with-text-136x68.png)

