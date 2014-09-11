#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define TAILLE_MAX_NB 11
#define TAILLE_MAX 256

#include "letter2int.c"
#include "int2letter.c"

void ProcedureConversion_letter2int(char *Lettres, char *ResultatLettres);
void ProcedureConversion_int2letter(char *fichierNombres, char *fichierResultatNombres);

int main(int argc, char *argv[])
{
	int mode = 0;
	printf("Quelle conversion voulez-vous effectuer ?\n");
	printf("Entrez 1 pour letter2int\n");
	printf("Entrez 2 pour int2letter\n");
	printf("Votre choix ?\n");
	scanf("%d", &mode);
	switch(mode){
		case 1:
			ProcedureConversion_letter2int(argv[1], argv[2]);
			break;
		case 2:
			ProcedureConversion_int2letter(argv[1], argv[2]);
			break;
		default:
			printf("C'était 1 ou 2. Veuillez relancer le programme.");
			break;
	}
}


void ProcedureConversion_int2letter(char *ListeNombres, char *ResultatNombres){

/*déclaration des variables*/
	FILE *fichierLettre =NULL; 	   /*fichier contenant les nombres*/
	FILE *fichierNombre = NULL;	   /*fichier contenant le résultat en toute
						 lettre*/

	char Nombres[TAILLE_MAX_NB]={0}; 	   /*chaine contenant chaque nombre à la 
						lecture du fichier*/
	
/*Début de la fonction*/
	fichierNombre = fopen(ListeNombres, "r");
	fichierLettre = fopen(ResultatNombres ,"w");

	if (fichierNombre != NULL && fichierLettre != NULL){  /* on teste si l'ouverture des 2 
								fichiers à fonctionné */

		while (fgets(Nombres, TAILLE_MAX_NB, fichierNombre) != NULL){
							// tant que fgets ne retourne pas NULL, c'est qu'il y a une nombre à lire
							// on récupère le nombre lu sur chaque ligne en "char"
							// et on la converti en un nombre que l'on
							// passe dans la fonction de traduction "ChiffreEnLEttre"
							// on écrit le résultat dans le fichier

								// on envoi la ligne récupérée par fgets convertie en nombre 
								// à la fonction de conversion, on récupère la chaine résultat "c"
			
			fprintf(fichierLettre, "%s\n", int2letter(atoi(Nombres)));	
								// la chaine "c" contenant le nombre est écrite dans le fichier
								// "ResultatNombres.txt"
		}
		
		fclose(fichierNombre);
		fclose(fichierLettre);
	}
}


void ProcedureConversion_letter2int(char *Lettres, char *ResultatLettres)
{
	
	FILE *fichierLettre =NULL; 	   /*fichier contenant les chiffres ecrit en lettre*/
	FILE *fichierNombre = NULL;	   /*fichier contenant les résultats sous forme d'une nombre*/

	char Phrase[TAILLE_MAX]=""; 	   /*chaine contenant chaque nombre (1 seule ligne) à la 
						lecture du fichier*/
		
	fichierLettre = fopen(Lettres,"r");
	fichierNombre = fopen(ResultatLettres, "w");
	
	if (fichierNombre != NULL && fichierLettre != NULL){  /* on teste si l'ouverture des 2 
									fichiers à fonctionné */
				
		while (fgets(Phrase, TAILLE_MAX, fichierLettre) != NULL){
							// tant que fgets ne retourne pas NULL, c'est qu'il y a une nombre à lire
							// on récupère le nombre lu sur chaque ligne en "char"
							// on  le passe dans la fonction de traduction "LettreEnChiffre"
							// on écrit le résultat dans le fichier "ResultatLettre.txt"

			Phrase[strlen(Phrase)-1]='\0';	// On remplace le dernier caractère de la chaine "/n" par le 
							//caractère de fin de chaine "/0"

			fprintf(fichierNombre, "%d\n",letter2int(Phrase));	// on envoi la ligne récupérée par fgets 
										// à la fonction de conversion. On récupère le 
									// résultat en entier, et on l'ecrit dans le fichier
									// "ResultatLettres.txt"
		}//fin While
		
		fclose(fichierNombre);		// on ferme les fichiers ouverts
		fclose(fichierLettre);
	}//fin du if
}
