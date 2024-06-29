# Chat de discussion UDP

## Description

Ce projet est un chat de discussion local utilisant le protocole UDP. Il permet à plusieurs clients connectés au même réseau de discuter en temps réel.

## Technologies utilisées

- Java avec la librairie `DatagramSocket` pour la communication en réseau

## Installation

1. **Cloner le dépôt Git** : Clonez ce dépôt sur votre machine locale en utilisant la commande git suivante :
```bash 
git clone git@github.com:Coaraa/ChatUDP.git
```

2. **Lancer le serveur** : Lancez une instance du serveur en exécutant le fichier `Main.java` situé dans le dossier `src`. Le port par défaut est le `25565`.

3. **Lancer un client** : Lancez une instance du client en exécutant le fichier `Main.java` situé dans le dossier `src`, en spécifiant l'adresse IP du serveur et le port utilisé dans les paramètres. Vous pouvez lancer plusieurs clients pour simuler une discussion entre plusieurs personnes. Lors de la connexion, vous devrez choisir un pseudo qui sera utilisé pour identifier vos messages. Il doit être unique parmi les clients connectés.

## Utilisation

La communication sur le serveur fonctionne par salle. Lorsqu’un client se connecte, il rejoint automatiquement la salle `générale`. Lorsque le client envoie un message, il est reçu par tous les clients connectés à la même salle précédée du pseudo de l’expéditeur.

Il existe plusieurs commandes que le client peut utiliser pour interagir avec le serveur. Les commandes sont préfixées par le caractère `/`. Voici la liste des commandes disponibles :
- **Envoyer un message privé** : `/msg <pseudo> <message>` : envoie un message privé au client dont le pseudo est spécifié.
- **Créer une salle** : `/room create <nom>` : crée une nouvelle salle avec le nom spécifié.
- **Rejoindre une salle** : `/room join <nom>` : rejoint la salle avec le nom spécifié.
- **Quitter une salle** : `/room leave` : quitte la salle actuelle pour rejoindre la salle `générale`.
- **Lister les salles et personnes connectées** : `/list` : affiche la liste des salles et des personnes connectées à la salle actuelle.
- **Se déconnecter** : `/quit` : déconnecte le client du serveur.
- **Miguel** : `/miguel` : fait apparaître Miguel dans la console.
- **Aide** : `/help` : affiche la liste des commandes disponibles.

Il est possible de consulter les logs du serveur dans le fichier `log.txt`.

## Auteurs

- [HONORÉ Alexandre](https://github.com/Tibouyou)
- [SITHIDEJ Clara](https://github.com/Coaraa)
