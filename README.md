# MMI Backend — README

API REST Spring Boot pour la plateforme portfolio étudiants du département MMI — IUT Marne-la-Vallée.

---

## Stack technique

| Technologie | Version | Rôle |
|---|---|---|
| Spring Boot | 3.2.3 | Framework principal |
| Spring Security | 6.x | Authentification & autorisation |
| Spring Data JPA | 3.x | Accès base de données |
| Spring Mail | 3.x | Envoi d'emails |
| JWT (jjwt) | 0.11.5 | Tokens d'authentification |
| H2 (dev) | — | Base en mémoire |
| MySQL (prod) | 8.x | Base de production |
| Lombok | — | Réduction boilerplate |
| Java | 21 | Langage |
| Maven | 3.9.x | Gestion des dépendances |

---

## Lancer le projet

```bash
# Compiler
mvn clean install -DskipTests

# Lancer
mvn spring-boot:run
```

Serveur démarré sur **http://localhost:8080**

### Console H2 (dev)
- URL : `http://localhost:8080/h2-console`
- JDBC URL : `jdbc:h2:mem:mmidb`
- User : `sa` | Password : *(vide)*

> Pour persister les données entre redémarrages, modifier dans `application.properties` :
> ```properties
> spring.datasource.url=jdbc:h2:file:./data/mmidb
> spring.jpa.hibernate.ddl-auto=update
> ```

---

## Configuration `application.properties`

```properties
# Base de données (dev H2)
spring.datasource.url=jdbc:h2:mem:mmidb
spring.datasource.username=sa
spring.datasource.password=

# JWT
app.jwt.secret=votre-secret-256-bits-minimum
app.jwt.expiration=7200000          # 2 heures en ms

# Upload fichiers
app.upload.dir=uploads
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=55MB

# Email (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=projettutore@gmail.com
spring.mail.password=votre-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Admin et frontend
app.admin.email=admin@mmi.fr
app.frontend.url=http://localhost:5173
```

---

## Comptes de test (créés automatiquement)

| Rôle | Email | Pseudo | Mot de passe |
|---|---|---|---|
| ADMIN | `admin@mmi.fr` | `admin` | `admin123` |
| PROF | `sophie.martin@mmi.fr` | `prof_sophie` | `prof123` |
| USER | `lucas@etudiant.fr` | `lucas_d` | `user123` |
| USER | `emma@etudiant.fr` | `emma_r` | `user123` |
| USER | `thomas@etudiant.fr` | `thomas_v` | `user123` |

---

## Endpoints API

### 🔐 Authentification — `/api/auth`

| Méthode | URL | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Inscription | ❌ |
| POST | `/api/auth/login` | Connexion (email ou pseudo) | ❌ |

**Login — body :**
```json
{ "identifiant": "lucas@etudiant.fr", "password": "user123" }
```
**Réponse :**
```json
{ "token": "eyJ...", "role": "USER", "pseudo": "lucas_d", "userId": 3 }
```
→ Utiliser le token : `Authorization: Bearer <token>`

---

### 📁 Projets — `/api/projets`

| Méthode | URL | Description | Auth |
|---|---|---|---|
| GET | `/api/projets` | Projets validés (public) | ❌ |
| GET | `/api/projets/:id` | Détail d'un projet | ❌ |
| GET | `/api/projets/mes-projets` | Mes projets (tous statuts) | ✅ |
| GET | `/api/projets/mes-likes` | Projets likés par l'utilisateur | ✅ |
| GET | `/api/projets/:id/liked` | L'utilisateur a-t-il liké ? | ✅ |
| GET | `/api/projets/auteur/:userId` | Projets validés d'un auteur | ❌ |
| POST | `/api/projets` | Créer un projet | ✅ |
| PUT | `/api/projets/:id` | Modifier un projet | ✅ |
| DELETE | `/api/projets/:id` | Supprimer un projet | ✅ |
| PATCH | `/api/projets/:id/like` | Liker / déliker | ✅ |

**Body création/modification :**
```json
{
  "titre": "Mon projet",
  "description": "...",
  "matiere": "WEB",
  "competition": true,
  "thumbnailUrl": "/uploads/thumbnails/uuid.jpg",
  "fichierUrl": "/uploads/fichiers/uuid.pdf",
  "fichiersUrls": ["/uploads/fichiers/a.pdf", "/uploads/fichiers/b.mp4"],
  "liensUrls": ["https://mon-site.fr", "https://github.com/..."],
  "urlMedia": "https://mon-site.fr",
  "equipeIds": [2, 4]
}
```

**Matières disponibles :** `UX` `UI` `WEB` `TROIS_D` `DESIGN` `MULTIMEDIA` `AUTRE`

**Logique métier :**
- USER → statut `EN_ATTENTE` + email admin envoyé
- PROF / ADMIN → statut `VALIDE` direct
- Modification par USER → repasse `EN_ATTENTE`
- Modification par PROF/ADMIN → reste `VALIDE`
- Like/délike : 1 like max par utilisateur, persisté dans `projet_likers`

---

### 💬 Commentaires — `/api/projets/:projetId/commentaires`

| Méthode | URL | Description | Auth |
|---|---|---|---|
| GET | `/api/projets/:id/commentaires` | Lire les commentaires | ❌ |
| POST | `/api/projets/:id/commentaires` | Poster un commentaire | ✅ |
| DELETE | `/api/projets/:id/commentaires/:comId` | Supprimer un commentaire | ✅ |

---

### 👤 Utilisateurs — `/api/users`

| Méthode | URL | Description | Auth |
|---|---|---|---|
| GET | `/api/users/me` | Mon profil | ✅ |
| PUT | `/api/users/me` | Modifier mon profil (partiel) | ✅ |
| PUT | `/api/users/me/avatar` | Changer mon avatar | ✅ |
| PUT | `/api/users/me/liens` | Gérer mes liens profil (max 4) | ✅ |
| GET | `/api/users/mes-commentaires` | Mes commentaires postés | ✅ |
| GET | `/api/users/profil/:pseudo` | Profil public par pseudo | ❌ |
| GET | `/api/users/search?q=lucas` | Recherche d'étudiants (role=USER) | ✅ |

**Body avatar :**
```json
{ "avatarType": "emoji", "avatarValue": "🐱" }
{ "avatarType": "image", "avatarValue": "/uploads/avatars/uuid.jpg" }
{ "avatarType": "", "avatarValue": "" }
```

**Body liens profil :**
```json
[
  { "titre": "GitHub", "url": "https://github.com/pseudo" },
  { "titre": "Portfolio", "url": "https://mon-portfolio.fr" }
]
```

---

### 📤 Upload — `/api/upload`

| Méthode | URL | Description | Auth |
|---|---|---|---|
| POST | `/api/upload/thumbnail` | Upload miniature (jpg/png/webp, 5 Mo) | ✅ |
| POST | `/api/upload/fichier` | Upload fichier projet (pdf/mp4/glb/gltf, 50 Mo) | ✅ |
| POST | `/api/upload/avatar` | Upload photo de profil (jpg/png/webp/gif, 2 Mo) | ✅ |

Les fichiers sont servis statiquement via `/uploads/**` (accessible sans auth).

**Validations (toutes les routes upload) :**
- Extension autorisée (liste blanche)
- Taille maximale
- Magic bytes (en-têtes binaires du fichier)

---

### 🔔 Notifications — `/api/notifications`

| Méthode | URL | Description | Auth |
|---|---|---|---|
| GET | `/api/notifications` | Mes notifications | ✅ |
| GET | `/api/notifications/count` | Nombre de non-lues | ✅ |
| PATCH | `/api/notifications/:id/lire` | Marquer comme lue | ✅ |
| PATCH | `/api/notifications/lire-tout` | Tout marquer comme lu | ✅ |
| POST | `/api/notifications/signalement` | Signaler un commentaire | ✅ |
| POST | `/api/notifications/admin-message` | Message admin → auteur commentaire | ✅ ADMIN |
| POST | `/api/notifications/equipe` | Notifier un membre d'équipe ajouté | ✅ |

**Types de notifications :**

| Type | Déclencheur | Destinataire |
|---|---|---|
| `PROJET_VALIDE` | Admin valide un projet | Auteur du projet |
| `PROJET_REJETE` | Admin refuse un projet | Auteur du projet |
| `NOUVEAU_COMMENTAIRE` | Commentaire posté | Auteur du projet |
| `EQUIPE` | Ajout en équipe d'un projet | Membre ajouté |
| `SUPPRESSION_COMMENTAIRE` | Admin supprime un commentaire | Auteur du commentaire |
| `SIGNALEMENT` | Signalement d'un commentaire | Tous les admins |

---

### 🛡️ Administration — `/api/admin` *(ROLE_ADMIN requis)*

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/admin/users` | Lister tous les utilisateurs |
| GET | `/api/admin/users/:id` | Détail d'un utilisateur |
| POST | `/api/admin/users` | Créer un utilisateur (rôle forcé) |
| PUT | `/api/admin/users/:id` | Modifier un utilisateur |
| DELETE | `/api/admin/users/:id` | Supprimer un utilisateur |
| GET | `/api/admin/projets` | Tous les projets (tous statuts) |
| PATCH | `/api/admin/projets/:id/statut?statut=VALIDE` | Changer le statut d'un projet |
| GET | `/api/admin/users/:id/historique` | Historique d'actions d'un étudiant |

---

## Structure des entités

```
User
 ├── id, nom, prenom, pseudo, email, password (BCrypt)
 ├── role : USER | PROF | ADMIN
 ├── classePromo, telephone, adresse
 ├── matiereEnseignee (PROF), specialisation (ADMIN)
 ├── avatarType, avatarValue
 ├── liensProfilJson (TEXT — JSON array)
 └── → projets[], commentaires[], actions[]

Projet
 ├── id, titre, description, datePublication
 ├── matiere, statut : EN_ATTENTE | VALIDE | REJETE
 ├── competition (boolean), likes
 ├── thumbnailUrl, fichierUrl, urlMedia
 ├── fichiersUrls[] (@ElementCollection)
 ├── liensUrls[] (@ElementCollection)
 ├── likers[] (@ManyToMany → User)
 ├── equipe[] (@ManyToMany → User)
 └── → auteur (User), commentaires[], actions[]

Commentaire
 ├── id, titre, contenu, dateHeure
 ├── → auteur (User), projet (Projet)

Notification
 ├── id, type, titre, message, lu, dateHeure
 ├── projetId
 └── → destinataire (User)

ActionLog
 ├── id, typeAction, description, dateHeure
 └── → user (User), projet (Projet)
```

---

## Sécurité

- Authentification **JWT Bearer Token** — expiration 2h (`app.jwt.expiration=7200000`)
- Mots de passe hashés en **BCrypt**
- Routes publiques : `GET /api/projets/**`, `GET /api/users/profil/**`, `/uploads/**`, `/api/auth/**`
- Routes admin : `hasRole('ADMIN')` via `@PreAuthorize`
- Validation fichiers : extension + taille + **magic bytes** côté backend
- CORS configuré pour `localhost:5173` et `localhost:3000`
- `@JsonIgnore` sur `password` — jamais sérialisé dans les réponses

---

## Emails automatiques

Envoyés à l'admin lors des actions USER sur les projets :

| Événement | Sujet | Contenu |
|---|---|---|
| Soumission projet | Nouveau projet en attente | Pseudo + titre + lien `/admin/validation` |
| Modification projet | Projet modifié — validation requise | Pseudo + titre + lien `/admin/validation` |
| Suppression projet | Projet supprimé | Pseudo + titre + lien `/admin/validation` |

> En dev sans SMTP configuré, les emails sont loggés en console (exception silencieuse).

---

## Structure des dossiers

```
src/main/java/com/mmi/
├── config/
│   ├── DataInitializer.java      ← Données de test au démarrage
│   ├── SecurityConfig.java       ← CORS, JWT filter, règles d'accès
│   └── WebMvcConfig.java         ← Servir /uploads/** statiquement
├── controller/
│   ├── AuthController.java
│   ├── ProjetController.java
│   ├── CommentaireController.java
│   ├── UserController.java
│   ├── AdminController.java
│   ├── NotificationController.java
│   ├── FileUploadController.java
│   └── AvatarUploadController.java
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── ProjetRequest.java
│   └── CommentaireRequest.java
├── entity/
│   ├── User.java
│   ├── Projet.java
│   ├── Commentaire.java
│   ├── Notification.java
│   └── ActionLog.java
├── enums/
│   ├── Role.java                 ← USER | PROF | ADMIN
│   ├── Matiere.java              ← WEB | UI | UX | TROIS_D | DESIGN | MULTIMEDIA | AUTRE
│   └── ProjetStatut.java         ← EN_ATTENTE | VALIDE | REJETE
├── repository/
│   ├── UserRepository.java
│   ├── ProjetRepository.java
│   ├── CommentaireRepository.java
│   ├── NotificationRepository.java
│   └── ActionLogRepository.java
├── security/
│   ├── JwtUtil.java              ← Génération et validation des tokens
│   └── JwtAuthFilter.java        ← Filtre HTTP injecté avant UsernamePassword
├── service/
│   ├── AuthService.java
│   ├── ProjetService.java
│   ├── CommentaireService.java
│   ├── NotificationService.java
│   ├── EmailService.java
│   ├── FileStorageService.java   ← Stockage + validation magic bytes
│   └── UserDetailsServiceImpl.java
└── MmiApplication.java
```

---

## Tables générées automatiquement (H2 / MySQL)

| Table | Description |
|---|---|
| `users` | Utilisateurs |
| `projets` | Projets |
| `commentaires` | Commentaires |
| `notifications` | Notifications |
| `action_logs` | Journal d'activité |
| `projet_fichiers` | URLs fichiers joints (@ElementCollection) |
| `projet_liens` | URLs liens (@ElementCollection) |
| `projet_likers` | Relation ManyToMany user↔projet (likes) |
| `projet_equipe` | Relation ManyToMany user↔projet (équipe) |
| `uploads/` | Dossier physique : `thumbnails/`, `fichiers/`, `avatars/` |
