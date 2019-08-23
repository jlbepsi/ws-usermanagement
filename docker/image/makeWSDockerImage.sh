#!/bin/bash

# Variables
BUILDDIR="../../build"
SRCDIR="../../src"

# Copie du jar dans le repertoire target pour construire l'image apres
# ATTENTION il ne doit y avoir qu'un seul jar
COUNT=$(ls ${BUILDDIR}/libs/ws-usermanagement*.jar | wc -l)
if [ $COUNT -gt 1 ]
then
	echo "Il ne doit y avoir qu'un  seul fichier jar dans le répertoire de build"
	return
fi

cp ${BUILDDIR}/libs/ws-usermanagement*.jar target/ws-usermanagement.jar
# Copie du fichier de configuration
cp ${SRCDIR}/main/resources/application.properties target/
# Copie des clés
cp ${SRCDIR}/main/resources/keys/private_key.der target/keys/private_key.der
cp ${SRCDIR}/main/resources/keys/public_key.der target/keys/public_key.der


echo "Arret des containers"
# Liste des containers en cours
LISTIDS=$(docker ps -aqf "name=ws-user")
# Si la liste n'est pas vide ...
if [ ! -z $LISTIDS ] 
then
	# ... on arrete les containers
	docker container stop $(docker ps -aqf "name=ws-user")
fi

echo "Suppression des containers"
# Suppression du container si il existe
LISTIDS=$(docker ps -aqf "name=ws-user")
if [ ! -z $LISTIDS ] 
then
	docker container rm $(docker ps -aqf "name=ws-user")
fi

echo "Suppression de l'image"
# Suppression de l'image
LISTIDS=$(docker images -q epsi/ws-users)
if [ ! -z $LISTIDS ] 
then
	docker rmi $(docker images -q epsi/ws-users)
fi


echo "Création de l'image"
# Créer l'image Docker
docker build -t epsi/ws-users .


echo "Démarrage du container"
docker run -p 8081:8081 --detach --mount type=bind,source=/home/users/ldap,target=/home/users/ldap --restart always --name ws-user epsi/ws-users
