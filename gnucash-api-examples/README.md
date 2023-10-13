# Notes on the Example Programs
## What the Programs are
Theses programs are simple *example* programs demonstrating the correct / suggested use of the GnuCash API library, partially replacing a user documentation that in the current version does not exist.

## What the Programs are Not
* They are *not* general-purpose tools. 

  They could / might one day be the base for tools (and we hold that they are a good base), but there would still be some work to be done (not only technical work, but also what in the business world would be called specification work: What exactly is needed, what are the *use cases* (for the older guys like the author of this document) or the *user stories* (for the younger ones) resp.

* They are *not* test cases.

  Granted, the programs partially refer to the test data GnuCash file (which normally should not be the case, but things are not cleanly separated yet in this stage of development), and granted, one could -- within reason -- see parts of the programs as some sort of "high-level" test cases. However, in order to really be test cases, they would have to be embedded in a test environment, expected outcomes would have to be formally specified (and checked!) and some more things. 

  Test cases are where they belong to: in the GnuCash API library module, under the directory `test`.

## How To Use Them
Each of these programs contains a section for test data. It has to be adapted to your needs, especially to the GnuCash file that you are going to use it with.

[ Part of the test data (e.g., the account IDs) refers to the data in the test data GnuCash file, but that does not mean that they actually have to / should be used with this file. Instead, the references are only used to clarify the meaning of the parameter / variable, if appropriate. ]

Each of the example programs compiles as it is (the authoer always gets mad when he sees example code in other projects that does not even compile, let alone work). And yes, they actually work, because they are not just code snippets, but complete, self-contained programs, and they actually have been tried out.

Thus, the suggested workflow is:

1. Copy copy one of them into your own project.
2. Adapt the data to the GnuCash file that you are using.
3. Compile.
4. Do a test run and see whether it actually does what you expect.
5. Adapt the code to your needs / integrate it into your project.

