# Technical Aspects
The test file has been generated with Gnucash V. 5.4.

Please note that the file contains *german* data, and not only superficially (i.e., german account names etc.), but also under the hood: GnuCash stores some (semi-)internal information in locale-specific form. This file has been generated on a german system (locale de_DE), and it probably should not be used / changed on a system with another locale.

[ In future releaes, we will therefore probably provide one test data file per supported locale. ]

When you change the test.gnucash file, please save it in 
*uncompressed* XML format.

# Testing Aspects
Please be careful when making changes on the file: All JUnit test cases heavily rely on it, and you might break things.

# Accounting Aspects
If you are an accountant (or have a similar profession), you might look at this file with a mixture of astonishment and disgust, perhaps thinking: "My goodness! Don't these guys know how to do accounting? This is awful nonsense!".

If you caught yourself having similar thoughts, you might want to give it a second thought: You see, you are right, but it's irrelevant (yes, you read that right). This is a *test* file, and it has neither been generated to be certified by public accountants nor does it provide a template on "how to do it right". Instead, it has been generated to provide a great variety of data for various test cases, some combinations of which might -- in real life -- occur very seldomly or perhaps even never. 

The author of these test cases and data has, in his professional career, made the experience that testing software often requires thinking on a higher abstraction level. This means that in test cases, you sometimes have to be relaxed about the specifics of the test data (and sometimes, they simply don't matter at all). 

So, if you are a non-technical guy/gal, perhaps an accountant, chances are that you are not trained in this kind of thinking and therefore struggle not to look too deeply at the data. In that case, please hold your horses, don't suggest to make "corrections" on the file and don't start to lecture the author on the matter; He does have some basic knowledge of accounting (enough to be well aware of the fact that the file contains nonsense), but not more. However, he does have a lot of experience in software-testing, and that's what matters here.