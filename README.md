# StanforNER Lab Project #

### What is this repository for? ###

This Lab use StanfordNER to Named Entity Recognition:
https://nlp.stanford.edu/software/CRF-NER.shtml

Features for CRF: 
https://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/edu/stanford/nlp/ie/NERFeatureFactory.html

### Datasets ###

* CoNLL2002 - Spanish

# Lab Project Guide #

Add to project classpath the folder stanford-ner to use on IDE. And run main.class

## To generate new classifiers/models
Open terminal, on classifiers folder and use:

## Train and Test (No model or classifier needed)
```
java -Xmx2048m -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -trainFile esp_train_full.tsv -testFile esp_test.tsv -macro > predictions.txt
```

## Generate classifier (define properties of model)
```
java -Xmx4096m -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -prop esp.prop
```

## Test with a generated classifier/model
with train dataset:
```
java -Xmx2048m -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier ner-conll2002-esp-model.ser.gz -testFile esp_train.tsv
```
with test dataset:
```
java -Xmx2048m -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier ner-conll2002-esp-model.ser.gz -testFile esp_test.tsv
```

# Lab Project Structure #

Classifiers: Check folder "classifiers" to see prop files and datasets used to create models
Models: Check folder "classifiers/model" to see avaible classifiers (you can use classifiers from stanford-ner 3,4,7 classes)
Results: Check folder "results" to see output results and predictions
