## ch-methylation 

[![Build Status](https://travis-ci.org/mischalundberg/ch-methylation.svg?branch=master)](https://travis-ci.org/mischalundberg/ch-methylation)

Contact: mischa.lundberg at mater.uq.edu.au

*Tool for analysing CH-Methylation status of several sequences at once*

# Prerequisites:

|what | where | why |
|-----|-------|-----|
|java SE1.7 or newer | http://www.oracle.com/technetwork/java/javase/downloads/index.html | Runtime environment |

# Documentation:

Main dialog : 

![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/gfx/main.PNG "Main dialog")

Drag and Drop your files : 

![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/gfx/drag_n_drop.png "Drag and Drop your files")

Click calculate and see what happens : 

![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/gfx/calculated.PNG "Click calculate and see what happens")

The output files will be stored in the same dir as your first dataset comes from : 

![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/gfx/output_files.PNG "The output files")

The Word file contains the complete sequences with Methylated nucleotides highlighted: 

![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/gfx/word.png "The Word file")

The Excel file contains 
* Path of Alignment file 
* Name of file 
* Position of methylated nucleotide 
* Distance to last methylated nucleotide (or to the start) 
* Sequence +/- 4 nucleotides from the methylated nucleotide 
* The preset cut-off for insignifant methylation stages 
* Count of methylated nucleotides over the spartial sequences 
* Number of nucleotides 
* Percent of methylation 
 
![picture alt](https://github.com/MischaLundberg/ch-methylation/blob/master/gfx/excel.PNG "The Excel file contains the partial sequences with Methylated nucleotides highlighted")

# Steps of further development:

* Reading .ab1 files
* Showing chromatograph for selecting parts to exclude
* Automaticaly detect parts to exclude

# Report an Issue
If you have any questions or suggestion, please feel free to contact me (mischa.lundberg at mater.uq.edu.au), create an issue or send a pull request 
