# TP2 - Compréhension des Programmes : Analyse du Couplage et Modularisation

Ce projet est une soumission pour le cours **HAI9131 - Évolution et Restructuration des Logiciels** de l'Université de Montpellier.

## Objectif du Projet

L'objectif principal de cet outil est d'effectuer une analyse statique sur le code source d'un projet Java afin d'en faciliter la compréhension. Pour ce faire, il accomplit les tâches suivantes :

1.  **Calcul des Métriques de Couplage :** Il quantifie le degré d'interdépendance entre les classes en analysant les appels de méthode.
2.  **Génération d'un Graphe de Couplage :** Il visualise les relations entre les classes sous la forme d'un graphe pondéré, où les poids des arêtes représentent la force du couplage calculée.
3.  **Identification de Modules par Clustering Hiérarchique :** Il utilise les données de couplage comme mesure de similarité et applique un algorithme de clustering hiérarchique pour regrouper automatiquement les classes fortement couplées.
4.  **Visualisation des Résultats :** Le résultat final du processus de clustering est affiché sous la forme d'un dendrogramme graphique, offrant une vue claire des modules architecturaux potentiels au sein du logiciel analysé.

L'outil prend en charge deux moteurs d'analyse distincts, comme requis par le cahier des charges : **Eclipse JDT** et **Spoon**.

## Comment Compiler et Exécuter le Projet

Ce projet est géré par Apache Maven, qui s'occupe de toutes les dépendances et du processus de compilation.

### Prérequis

*   **Java Development Kit (JDK) 17** doit être installé et configuré.
*   **Apache Maven** doit être installé et disponible dans le PATH du système.

### Compilation du Projet

1.  Ouvrez un terminal ou une invite de commandes et naviguez jusqu'au répertoire racine du projet.
2.  Exécutez la commande Maven suivante. Elle compilera le code source et empaquettera l'application dans un fichier JAR exécutable unique avec toutes ses dépendances.

    ```bash
    mvn clean package
    ```

3.  Une fois la compilation terminée avec succès, le fichier JAR exécutable, nommé `software-comprehension-1.0-SNAPSHOT-jar-with-dependencies.jar`, sera situé dans le répertoire `target/`.

### Exécution de l'Application

1.  Depuis votre terminal, naviguez jusqu'au répertoire `target/` nouvellement créé.
2.  Lancez l'application en utilisant la commande `java -jar` :

    ```bash
    java -jar software-comprehension-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

3.  Le programme présentera un menu pour choisir le moteur d'analyse. Les chemins vers le projet à analyser (`Library-Management-System-JAVA-master`) et le JDK requis sont **codés en dur** dans `Main.java` pour plus de commodité et pour garantir une exécution fiable lors de la correction.

4.  Une fois l'analyse terminée, les résultats (incluant le graphe d'appels et le graphe de couplage) seront affichés dans la console, et une nouvelle fenêtre apparaîtra avec le dendrogramme final.