# Monstera

[![Travis CI build](https://img.shields.io/travis/ysads/monstera/master.svg?style=flat-square)](https://travis-ci.org/ysads/monstera)
[![codecov](https://img.shields.io/codecov/c/github/ysads/monstera?style=flat-square)](https://codecov.io/gh/ysads/monstera)

☘️An experimental project that aims to be a survival guide for people in love with plants and pets.

## Testing

The project uses [Midje](https://github.com/marick/Midje/) for a clearer and faster development. The following alternatives may be used to test the application:

- `$ lein midje` will run all tests.

- `$ lein midje namespace.*` will run only tests beginning with "namespace.".

- `$ lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
