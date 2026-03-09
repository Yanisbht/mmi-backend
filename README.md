# MMI Backend - Projet Tutoré

Backend SpringBoot pour le site MMI - API REST complète avec JWT.

## Stack
- **SpringBoot 3.2** + Spring Security + Spring Data JPA
- **Base de données** : H2 en mémoire (dev) / MySQL (prod)
- **Auth** : JWT (Bearer Token)
- **Mail** : JavaMailSender (SMTP)

---

## Lancer le projet

```bash
# 1. Compiler
mvn clean install -DskipTests

# 2. Lancer
mvn spring-boot:run
```

Le serveur démarre sur **http://localhost:8080**

### Console H2 (base de données en dev)
> http://localhost:8080/h2-console  
> JDBC URL : `jdbc:h2:mem:mmidb`  
> User : `sa` | Password : *(vide)*

---

## Comptes de test (créés automatiquement au démarrage)

| Rôle    | Email                    | Pseudo         | Password   |
|---------|--------------------------|----------------|------------|
| ADMIN   | admin@mmi.fr             | admin          | admin123   |
| PROF    | sophie.martin@mmi.fr     | prof_sophie    | prof123    |
| USER    | lucas@etudiant.fr        | lucas_d        | user123    |

---

## Endpoints

### 🔐 Auth (`/api/auth`)
| Méthode | URL                    | Description              | Auth |
|---------|------------------------|--------------------------|------|
| POST    | `/api/auth/register`   | Inscription              | ❌   |
| POST    | `/api/auth/login`      | Connexion (email ou pseudo) | ❌ |

**Login body :**
```json
{ "identifiant": "lucas@etudiant.fr", "password": "user123" }
```
**Réponse :**
```json
{ "token": "eyJ...", "role": "USER", "pseudo": "lucas_d", "userId": 3 }
```
→ Utiliser le token dans le header : `Authorization: Bearer <token>`

---

### 📁 Projets (`/api/projets`)
| Méthode | URL                        | Description                      | Auth       |
|---------|----------------------------|----------------------------------|------------|
| GET     | `/api/projets`             | Tous les projets validés         | ❌ Public  |
| GET     | `/api/projets/{id}`        | Détail d'un projet               | ❌ Public  |
| GET     | `/api/projets/mes-projets` | Mes projets                      | ✅ Token   |
| POST    | `/api/projets`             | Ajouter un projet                | ✅ Token   |
| PUT     | `/api/projets/{id}`        | Modifier un projet               | ✅ Token   |
| DELETE  | `/api/projets/{id}`        | Supprimer un projet              | ✅ Token   |
| PATCH   | `/api/projets/{id}/like`   | Liker un projet                  | ✅ Token   |

**Matières disponibles :** `UX`, `UI`, `WEB`, `TROIS_D`, `DESIGN`, `MULTIMEDIA`, `AUTRE`

---

### 💬 Commentaires (`/api/projets/{projetId}/commentaires`)
| Méthode | URL                                          | Auth       |
|---------|----------------------------------------------|------------|
| GET     | `/api/projets/{projetId}/commentaires`       | ❌ Public  |
| POST    | `/api/projets/{projetId}/commentaires`       | ✅ Token   |
| DELETE  | `/api/projets/{projetId}/commentaires/{id}`  | ✅ Token   |

---

### 👤 Profil (`/api/users`)
| Méthode | URL              | Description                    | Auth     |
|---------|------------------|--------------------------------|----------|
| GET     | `/api/users/me`  | Voir son profil                | ✅ Token |
| PUT     | `/api/users/me`  | Modifier son profil (partial)  | ✅ Token |

---

### 🛡️ Admin (`/api/admin`) — ROLE_ADMIN requis
| Méthode | URL                                  | Description                        |
|---------|--------------------------------------|------------------------------------|
| GET     | `/api/admin/users`                   | Lister tous les utilisateurs       |
| GET     | `/api/admin/users/{id}`              | Voir un utilisateur                |
| POST    | `/api/admin/users`                   | Créer un user/prof (avec rôle)     |
| DELETE  | `/api/admin/users/{id}`              | Supprimer un utilisateur           |
| GET     | `/api/admin/projets`                 | Tous les projets (y compris attente)|
| PATCH   | `/api/admin/projets/{id}/statut`     | Valider/Rejeter un projet          |
| GET     | `/api/admin/users/{id}/historique`   | Historique d'actions d'un étudiant |

**Statuts projet :** `EN_ATTENTE`, `VALIDE`, `REJETE`

---

## Logique métier
- Un **USER** qui ajoute/modifie un projet → statut `EN_ATTENTE` + email envoyé à l'admin
- Un **PROF** ou **ADMIN** qui ajoute/modifie → validation automatique (`VALIDE`)
- L'admin valide/rejette via `PATCH /api/admin/projets/{id}/statut`
- Chaque ajout/modification/suppression est enregistré dans `action_logs`

---

## Configuration mail
Éditer `application.properties` :
```properties
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-app   # App Password Gmail
app.admin.email=admin@mmi.fr
```
> En dev sans mail configuré, les notifications sont simplement logguées en console.

---

## Collection Postman
Importer le fichier `MMI_API.postman_collection.json` dans Postman.  
Les tokens sont **sauvegardés automatiquement** dans les variables de collection après chaque login.
