FROM jenkins/jenkins:2.176.2-slim

# install required plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# set docker label to master node
COPY init.groovy.d/* /usr/share/jenkins/ref/init.groovy.d/

# switch to root, in order to be able to install packages
USER root

# install docker
RUN apt-get update -qq \
 && apt-get install -qqy apt-transport-https ca-certificates curl gnupg2 software-properties-common mercurial \
 && curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - \
 && add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/debian \
   $(lsb_release -cs) \
   stable" \
 && apt-get update -qq \
 && apt-get install docker-ce -y

# add jenkins user to docker group
RUN usermod -aG docker jenkins

# switch back to jenkins user
USER jenkins
