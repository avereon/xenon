# Testing

Testing Xenon is both important and challenging. Important because automated
tests provided needed efficiency and confidence that features are working, and
continue to work, as expected. Challenging because Xenon is a capable software
framework, and it has many moving parts.

## Test Categories
There are four main test categories in Xenon:

* Standard unit tests
* FX component unit tests
* Initialized program tests
* Full program tests
  * User Experience tests

### Standard Unit Tests
Standard unit tests are exactly the title describes, a test for a single unit
of logic or functionality. What it does not describe is what a unit test should
not need. Unit tests should not need help from either the FX platform or the 
program framework. Both of those situations merit a different category. If you
find that your test either needs the FX platform or the program framework to 
execute, please consider reworking your code to not require either of move it
to a different category.

### FX Component Tests
Developing custom FX components can be tricky because there is a very clear line
between a simple unit test and the need for the FX platform. If the component is
not going to be rendered as part of the test, usually a simple unit test will
suffice. However, if you need to test how the component will be rendered, 
including how it will be laid out, you will need to start the FX platform for
your test.

### Basic Program Tests
When developing some logic or feature in the program, it is necessary to have
some parts of the program working in order for the feature to be tested. 
However, many features can be tested without starting the entire program. This
is particularly useful to keep tests running quickly. Most of the program 
services (also called managers) can be tested in this mode of operation. This 
includes the asset, index, product, update, settings and task managers.

### Full Program Tests
When developing high level features in the program, it is usually necessary for
all the capabilities of the program to be available. This is particularly true
when developing tools, since tools heavily rely on the underlying service for
much of their boilerplate functionality.

### User Experience Tests (also User Interface Tests)
User experience tests are a subset of full program tests, in that they start the
entire program, but are "driven" with user device input instead of API calls.
UX tests are valuable to ensure user experience quality, but take the longest of
all tests to run.
