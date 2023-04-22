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

:warning: **This version of CliniDeID is a beta release with a few minor issues to be corrected and resulting from the conversion from commercial software with usage-based licensing fee to free and open source software.**

## Getting Started


## Installation

CliniDeID requires Java JDK 1.7 or more recent to build and run. If needed, download and install a JDK like OpenJDK 17 or more recent (https://www.oracle.com/in/java/technologies/downloads/ or https://jdk.java.net/19/_ and install including JAVA_HOME setup etc. For Apple computers with M1/M2 processors, some Java library needs are different and the Azul Zulu OpenJDK builds work well ([download link](https://www.azul.com/core-post-download/?java=17&arch=ARM+64-bit&type=macos-dmg&sha=bd9757c8b157c86a9735bae04c76e94d704fa7985f7088a9291e933cd10a27af&url=https%3A%2F%2Fcdn.azul.com%2Fzulu%2Fbin%2Fzulu17.32.13-ca-fx-jdk17.0.2-macosx_aarch64.dmg&endpoint=zulu&cert=https%3A%2F%2Fcdn.azul.com%2Fzulu%2Fpdf%2Fcert.zulu17.32.13-ca-fx-jdk17.0.2-macosx_aarch64.dmg.pdf).

CliniDeID requires Python 3.8.7 or more recent to run.

Apache Maven is required to build this project: https://maven.apache.org/download.cgi.

You will need trained models for the machine learning modules in CliniDeID. Instructions on how to train your own models can be found in the data/models folder. Models were pre-trained with training subsets of the 2006 i2b2 de-identification challenge annotated corpus (https://portal.dbmi.hms.harvard.edu/projects/n2c2-2006/), the 2014 i2b2/UTHealth de-identification challenge annotated corpus (https://portal.dbmi.hms.harvard.edu/projects/n2c2-2014/) and 2016 CEGS N-GRID n2c2 de-identification challenge annotated corpus (https://portal.dbmi.hms.harvard.edu/projects/n2c2-2016/). They can be used **only for non-commercial applications** and downloaded from:

| Model | File name | Download link |
| ------ | ------ | ------ |
| RNN | rnn-U3-FullSplit.h5 | https://e.pcloud.link/publink/show?code=XZWr7jZ7a6Kn8TVRnRLeE1HQ5mjtS6uuW8y |
| SVM map | svmMap-U3-FullSplit | https://e.pcloud.link/publink/show?code=XZlr7jZhAtGksespVS7FP6mY9niAFbopAUV |
| SVM model | svmModel-U3-FullSplit | https://e.pcloud.link/publink/show?code=XZtr7jZs3pm2OR7PgmqCd4A8w2mLm2RlQM7 |
| MIRA | mira-U3-FullSplit | https://e.pcloud.link/publink/show?code=XZar7jZjNXjjyH1rBkr6nQ3kdE5iRNquOLV |

JavaFX is used in this project.

To build the package from the CliniDeID folder:
`mvn clean package -DskipTests`
(tests can be run but may fail due to missing data files)
`./scripts/makeDeployGenericZip.sh`
makes a zip file CliniDeID.zip that does not have the needed platform specific scripts. After that script has run, then run
`./scripts/makePlatformZip.sh Windows|Mac|CentOs|Ubuntu|RedHat`
to make a zip file CliniDeID-`OS`.zip 

The ZIP file contains a setupCliniDeID script to setup the installation and runCliniDeID and runCliniDeIDcommandLine to run the program in GUI or commandline mode. 

The project can be run from within IntelliJ with an included run configuration. It needs 28 GB heap space to run.

## Running CliniDeID

To start the GUI version of CliniDeID, run the runCliniDeID script
`./runCliniDeID.sh`

To start the command line version of CliniDeID, run the runCliniDeIDcommandLine script
`./runCliniDeIDcommandLine.sh`

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

Pretrained machine learning models can be used only for non-commercial applications.

