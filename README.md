<!--
This file is part of CliniDeID.
CliniDeID is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with CliniDeID. If not, see <https://www.gnu.org/licenses/>.
-->

# CliniDeID
## _Automatic clinical data de-identification_


[![GitHub license](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](https://www.gnu.org/licenses/gpl-3.0.txt)

CliniDeID automatically de-identifies clinical text notes according to the [HIPAA Safe Harbor method](https://www.hhs.gov/hipaa/for-professionals/privacy/special-topics/de-identification/index.html#standard). It accurately finds identifiers and tags or replaces them with realistic surrogates for better anonymity. It improves access to richer, more detailed, and more accurate clinical data for clinical researchers. It eases research data sharing, and helps healthcare organizations protect patient data confidentiality.
### Features
* Clinical text de-identification: Uses advanced artificial intelligence algorithms to accurately identify all mentions of identifiers (PII) in unstructured clinical text notes and replaces them with realistic surrogates (PII resynthesis) or tags, as desired. Does not rely on any known identifiers but can use known identifiers to double-check the PII identification if available. Generalizes well to most common types of clinical notes.
* Replacement of identified PII with realistic surrogates and consistently across the whole patient record (PII resynthesis), or with tags (generic or PII categories).
* Highly accurate identification of PII (as demonstrated in several peer-reviewed evaluations and comparisons; see related publications below)
* Multiple input and output data formats: plain text, HL7 CDA , relational databases (PostgreSQL , MySQL, DB2, etc.)

This version of CliniDeID was built to be used on-premises and includes a user-friendly graphical user interface and can also be run from the command line (details below).


## Overview

CliniDeID version with all machine learning models, RNN included. 

:warning: **For additional information, see the main branch.**


## License

GPL version 3.0 or later
![GPL](https://www.gnu.org/graphics/gplv3-with-text-136x68.png)

