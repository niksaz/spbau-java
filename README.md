# spbau-java

[![Build Status](https://travis-ci.org/niksaz/spbau-java.svg?branch=12-task%2B)](https://travis-ci.org/niksaz/spbau-java)
[![codecov.io](https://codecov.io/github/niksaz/spbau-java/coverage.svg?branch=12-task%2B)](https://codecov.io/github/niksaz/spbau-java?branch=12-task%2B)

MyGit -- VCS system.
Similar architecture to original Git. There are Commits, Branches, Trees, Blobs. Commit point to the roots of corresponding Trees. Branches contain links to Commits. Trees represent directories in filesystems, blobs -- files.
All files are identified by their hash code which is provided by SHA-1 algorithm.
