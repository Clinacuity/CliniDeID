#### Deployment
The files found in this directory are the files needed to launch
a new RHEL instance of the CliniDeID NLP Service. In AWS, we use
the Amazon Linux AMIs, which have different `yum` packages available.

A good starting point is the `init.sh` file, which follows the
entire process of installing all the requirements for running
the NLP pipeline as a service. Currently, this setup supports
a load-balanced backend, but pieces can be extracted to support
other architectures. 
