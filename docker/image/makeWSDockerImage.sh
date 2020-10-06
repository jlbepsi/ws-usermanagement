#!/bin/bash

echo "Arret des containers"
# Liste des containers en cours
LISTIDS=$(docker ps -aqf "name=ws-users")
# Si la liste n'est pas vide ...
if [ ! -z $LISTIDS ] 
then
	# ... on arrete les containers
	docker container stop $(docker ps -aqf "name=ws-users")
fi

echo "Suppression des containers"
# Suppression du container si il existe
LISTIDS=$(docker ps -aqf "name=ws-users")
if [ ! -z $LISTIDS ] 
then
	docker container rm $(docker ps -aqf "name=ws-users")
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
docker run -p 8081:8080 \
	--mount type=bind,source=/home/users/ldap,target=/home/users/ldap \
	--mount type=bind,source=/docker/server/wsusermanagement/logs/,target=/logs \
	--detach  --restart always --name ws-users epsi/ws-users