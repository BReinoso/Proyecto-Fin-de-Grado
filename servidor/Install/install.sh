#!/bin/bash
# This script must be executed with the comman ". ./install.sh" on your terminal and it must be executed with sudo
# See https://github.com/BVLC/caffe/wiki/Ubuntu-14.04-VirtualBox-VM if you need it
cd ~
#install git to clone from github
apt-get install git
#clone the student's project
git clone https://github.com/garfio1/Proyecto-Fin-de-Grado.git
#Instal build essentials
apt-get install build-essential
#Install latest version of kernel headers
apt-get install linux-headers-`uname -r`
#To download nvidia drivers
#Uncoment if you are in a virtual machine, then wont install the drivers
#sudo apt-get install curl
#cd ~/Downloads/
#curl -O "http://developer.download.nvidia.com/compute/cuda/6_5/rel/installers/cuda_6.5.14_linux_64.run"
#chmod +x cuda_6.5.14_linux_64.run
#./cuda_6.5.14_linux_64.run --kernel-source-path=/usr/src/linux-headers-`uname -r`/
#Updating library path
#echo 'export PATH=/usr/local/cuda/bin:$PATH' >> ~/.bashrc
#echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/cuda/lib64:/usr/local/lib' >> ~/.bashrc
#source ~/.bashrc
#------------------------------------------------------------
#    Accept the EULA
#    Do NOT install the graphics card drivers (since we are in a virtual machine)
#    Install the toolkit (leave path at default)
#    Install symbolic link
#    Install samples (leave path at default)
#------------------------------------------------------------
#Install dependencies of Caffe
apt-get install -y libprotobuf-dev libleveldb-dev libsnappy-dev libopencv-dev libboost-all-dev libhdf5-serial-dev protobuf-compiler gfortran libjpeg62 libfreeimage-dev libatlas-base-dev git python-dev python-pip libgoogle-glog-dev libbz2-dev libxml2-dev libxslt-dev libffi-dev libssl-dev libgflags-dev liblmdb-dev python-yaml
easy_install pillow
#Clonning Caffe
cd ~
git clone https://github.com/BVLC/caffe.git

#Install python dependencies
cd caffe
cat python/requirements.txt | xargs -L 1 sudo pip install
#Symbolic links
sudo ln -s /usr/include/python2.7/ /usr/local/include/python2.7
sudo ln -s /usr/local/lib/python2.7/dist-packages/numpy/core/include/numpy/ /usr/local/include/python2.7/numpy
#Cambiando el Makefile.config
chmod -R 777 ~/caffe
rm ~/caffe/Makefile.config
cp ~/Proyecto-Fin-de-Grado/servidor/Install/Makefile.config ~/caffe
#Build caffe
make pycaffe
make matcaffe
make all
make test
#Clone NeuralTalk
cd ~
git clone https://github.com/karpathy/neuraltalk.git
#Install server dependencies
pip install beautifulsoup
pip install flask

