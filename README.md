## ch-methylation 

[![Build Status](https://travis-ci.org/mischalundberg/ch-methylation.svg?branch=master)](https://travis-ci.org/mischalundberg/ch-methylation)

Contact: mischa.lundberg@mater.uq.edu.au

*Tool for analysing CH-Methylation status of several sequences at once*

# Prerequisites:

|what | where | why |
|-----|-------|-----|
|java SE1.7 or higher | http://www.oracle.com/technetwork/java/javase/downloads/index.html | Runtime environment |

# Documentation:

Main dialog : ![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/main.PNG "Main dialog")

Drag and Drop your files : ![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/drag_n_drop.png "Drag and Drop your files")

Click calculate and see what happens : ![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/calculated.PNG "Click calculate and see what happens")

The output files : ![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/output_files.PNG "The output files")

The Word file contains the complete sequences with Methylated nucleotides highlighted: ![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/word.png "The Word file")

The Excel file contains 
* Path of Alignment file 
* Name of file 
* Position of methylated nucleotide 
* Distance to last methylated nucleotide (or to the start) 
* sequence +/- 4 nucleotides from the methylated nucleotide 
* the preset cut-off for insignifant methylation stages 
* Count of methylated nucleotides over the spartial sequences 
* # of nucleotides 
* % methylated 
: ![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/excel.png "The Excel file contains the partial sequences with Methylated nucleotides highlighted")

# Steps of further development:

* Reading .ab1 files
* Showing chromatograph for selecting parts to exclude
* Automaticaly detect parts to exclude
