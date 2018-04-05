Plan presentation slug avril 2018


Future


Presentation général. Valeur en devenir. eager, async

opération courante. map, flatmap, zip

-  Liste de future vers future de listes. Future.sequence, Future.traverse

 Ne bloquez pas pour attendre le résultat d’une Future.  Ne jamais mettre d’Await, si vous le faites quand même mettez au moins un timeout.

Eager => nécessite un execution context

presentation de scala.concurrent.ExecutionContext.Implicit.global

Opération bloquantes

 Si on les executes dans le même execution contexte => on va bloquer, ralentir notre application
Pour mieux faire on peut les executer dans un bloc scala.concurrent.blocking spécifiant un block context.
Ça a des limitation.
Le mieux, utiliser un thread pool different pour les io, que pour le reste de l’application.



Task



 Presentation générale. Lazy, async. Factory de Future
Création d’une task
Batching 
Execution context ( scheduler requis uniquement au moment de lancer l’execution)
Specification d’un scheduler spécifique 
Memoisation 
Cancelable/nettoyage des ressources



Conclusion

Par rapport aux Futures, les Tasks Monix permettent une gestion plus fine sur l’exécution du programme. Permettant une meilleure comprehension ainsi que de meilleurs performances si bien utilisées. 

