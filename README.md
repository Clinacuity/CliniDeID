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

CliniDeID requires Java 17 to run and Maven 3 and Java 17 JDK and Java FX Sdk to build

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

Java 17 and maven 3 are required. 
Models are separate from the repository due to size. They must be download and placed into the data/models directory. There should be a mira and svm subdirectory within data/models. The models can be download from ????


mvn clean package -DskipTests will build the jar
./scripts/makeDeployGenericZip.sh    will create a generic zip file designed to be used by the next script
./scripts/makePlatformZip.sh  _OS_ will create a zip for Windows, Mac, Ubuntu, Redhat, or CentOs with _OS_ parameter determining which is made.

To run from within intelliJ javaFx will need to be installed and its library added as well as adding this to the command line in the run configuration:
--module-path pathToJavaFxSDK/javafx-sdk-17.0.7/lib --add-modules=javafx.controls
and setting the VM to use 28GB heap memory (-Xmx28g)

```sh
gulp build dist --prod
```
## Publications
1. Meystre S, Petkov V, Silverstein J, Savova G, Malin B. De-Identification of Clinical Text: Stakeholders’ Perspectives and Acceptance of Automatic De-Identification. AMIA Annu Symp Proc. 2020. p. 124–6. 
2. Meystre S. CliniDeID for Clinical Text De-Identification. AMIA Annu Symp Proc. 2020. 
3. Kim Y, Meystre S. Improving De-identification of Clinical Text with Contextualized Embeddings. AMIA Annu Symp Proc. 2020:1813. 
4. Kim Y, Heider P, Meystre S. Comparative Study of Various Approaches for Ensemble-based De-identification of Electronic Health Record Narratives. AMIA Annu Symp Proc. 2020:648–57. 
5. Heider P, Obeid JS, Meystre S. A Comparative Analysis of Speed and Accuracy for Three Off-the-Shelf De-Identification Tools. AMIA Jt Summits Transl Sci Proc. 2020:241–50. 
6. Underwood G, Trice A, Kim Y, Accetta JK, Meystre S. Text De-Identification Impact on Subsequent Machine Learning Applications. In: AMIA Annu Symp Proc. 2019:1795. 
7. Obeid JS, Heider PM, Weeda ER, Matuskowitz AJ, Carr CM, Gagnon K, et al. Impact of De-Identification on Clinical Text Classification Using Traditional and Deep Learning Classifiers. Studies in health technology and informatics. 2019 Aug;264:283–7. 
8. Kim Y, Meystre SM. Voting Ensemble Pruning for De-identification of Electronic Health Records. AMIA Jt Summits Transl Sci Proc. 2019:1083. 
9. Meystre S, Heider P, Heider, Kim Y, Trice A, Underwood G. Clinical Text Automatic De-Identification to Support Large Scale Data Reuse and Sharing: Pilot Results. AMIA Annu Symp Proc 2018:2069.
10. Meystre S, Carrell D, Hirschman L, Aberdeen J, Fearn P, Petkov V, et al. Automatic Text De-Identification: How and When is it Acceptable? AMIA Annu Symp Proc 2018:124–6.
11. Kim Y, Heider P, Meystre SM. Ensemble-based Methods to Improve De-identification of Electronic Health Record Narratives. AMIA Annu Symp Proc 2018:663–72.
12. AAlAbdulsalam AK, Meystre SM. Learning to De-Identify Clinical Text with Existing Hybrid Tools. AMIA Summits Transl Sci Proc. 2017:150–1. 
13. Meystre SM, Ferrandez O, Friedlin FJ, South BR, Shen S, Samore MH. Text de-identification for privacy protection: a study of its impact on clinical text information content. Journal of biomedical informatics. 2014 Aug;50:142–50. 
14. Meystre S, Shen S, Hofmann D, Gundlapalli A. Can Physicians Recognize Their Own Patients in De-identified Notes? Stud Health Technol Inform. 2014;205:778–82. 
15. Meystre S, H D, Aberdeen J, Malin B. Automatic Clinical Text De-Identification: Is It Worth It, and Could It Work for Me? Medinfo. 2013. 
16. Ferrandez O, South BR, Shen S, Friedlin FJ, Samore MH, Meystre SM. BoB, a best-of-breed automated text de-identification system for VHA clinical documents. J Am Med Inform Assoc. 2013 Jan;20(1):77–83. 
17. Ferrandez O, South BR, Shen S, Friedlin FJ, Samore MH, Meystre SM. Evaluating current automatic de-identification methods with Veteran’s health administration clinical documents. BMC medical research methodology. 2012 Jul;12(1):109. 
18. South B, Shen S, Maw M, Ferrandez O, Friedlin FJ, Meystre S. Prevalence Estimates of Clinical Eponyms in De-Identified Clinical Documents. AMIA Summits Transl Sci Proc 2012:136.
19. Friedlin FJ, South B, Shen S, Ferrandez O, Nokes N, Maw M, et al. An Evaluation of the Informativeness of De-identified Documents. AMIA Summits Transl Sci Proc 2012:128. 
20. Ferrandez O, South B, Shen S, Maw M, Nokes N, Friedlin FJ, et al. Striving for Optimal Sensitivity to De-identify Clinical Documents. AMIA Summits Transl Sci Proc 2012:117.
21. Ferrandez O, South BR, Shen S, Friedlin FJ, Samore MH, Meystre SM. Generalizability and comparison of automatic clinical text de-identification methods and resources. AMIA Annu Symp Proc. 2012:199–208. 
22. Shen S, South B, Friedlin FJ, Meystre S. Coverage of Manual De-identification on VA Clinical Documents. AMIA Annu Symp Proc. 2011 Nov;1958. 
23. South B, Shen S, Friedlin FJ, Samore M, Meystre S. Enhancing Annotation of Clinical Text using Pre-Annotation of Common PHI. AMIA Annu Symp Proc 2010.
24. Meystre SM, Friedlin FJ, South BR, Shen S, Samore MH. Automatic de-identification of textual documents in the electronic health record: a review of recent research. BMC medical research methodology. 2010;10:70. 


## Funding
CliniDeID's development was funded in part by the U.S. NIGMS (R41GM116479 and R42GM116479). We thank Youngjun Kim and all software developers and engineers at Clinacuity and the Medical University of South Carolina who made the application development and release possible.

## License

GPL version 3.0 or later
![GPL](https://www.gnu.org/graphics/gplv3-with-text-136x68.png)

