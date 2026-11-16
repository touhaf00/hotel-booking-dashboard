# Exécution locale avec Docker Compose

Ce guide décrit la construction, le démarrage et l’administration
locale de l’application Hotel Booking.

## Architecture

Deux services sont définis dans `docker-compose.yml` :

| Service | Fonction |
|---------|----------|
| `app` | Application Spring Boot exécutée avec Java 17 |
| `db` | Base PostgreSQL avec stockage persistant |

L’application est accessible sur le port 8080.
PostgreSQL est accessible par l’application via le réseau interne Docker.

## Prérequis

- Java 17.
- Maven.
- Docker avec Docker Compose v2.
- OpenSSL.
- `htpasswd` pour la création du compte administrateur.
- Un terminal Bash ou Zsh.

Toutes les commandes s’exécutent depuis la racine du projet.
Le port 8080 doit être disponible avant le démarrage.

## 1. Préparer les variables d’environnement

Les secrets sont enregistrés dans `.local/docker.env`.
Ce répertoire est exclu du suivi Git.

```bash
(
  umask 077
  mkdir -p .local

  if [ ! -s .local/jwt-secret ]; then
    openssl rand -base64 32 > .local/jwt-secret
  fi

  if [ ! -s .local/docker.env ]; then
    printf 'HOTEL_DB_PASSWORD=%s\nHOTEL_JWT_SECRET=%s\n' \
      "$(openssl rand -hex 24)" \
      "$(cat .local/jwt-secret)" > .local/docker.env
  fi

  chmod 600 .local/docker.env .local/jwt-secret
)
```

| Variable | Utilisation |
|----------|-------------|
| `HOTEL_DB_PASSWORD` | Mot de passe de connexion à PostgreSQL |
| `HOTEL_JWT_SECRET` | Clé de signature des jetons JWT, encodée en Base64 |

La préparation conserve les valeurs déjà présentes.
Les noms de l’utilisateur PostgreSQL et de la base sont définis
dans le service `db` du fichier Compose.

## 2. Construire et démarrer

Construire les modules Maven et exécuter les tests :

```bash
mvn clean verify
```

Valider la configuration Compose :

```bash
docker compose --env-file .local/docker.env config --quiet
```

Construire l’image et démarrer les services :

```bash
docker compose --env-file .local/docker.env up -d --build
```

Suivre le démarrage de l’application :

```bash
docker compose --env-file .local/docker.env logs -f app
```

Attendre le message `Started HotelBookingApplication`.
Utiliser Ctrl+C pour quitter l’affichage des logs.

Vérifier l’état des services :

```bash
docker compose --env-file .local/docker.env ps
```

États attendus :

- `db` : running et healthy.
- `app` : running.

## 3. Initialiser le compte administrateur

Lors de la première utilisation, créer un compte applicatif
disposant du rôle ADMIN.

Le bloc suivant demande un mot de passe, génère son hash BCrypt,
puis crée le compte dans la base du conteneur :

```bash
(
  set -e

  hotel_admin_line="$(htpasswd -n -B -C 12 admin)"
  hotel_admin_hash="${hotel_admin_line#admin:}"

  docker compose --env-file .local/docker.env exec -T \
    -e HOTEL_ADMIN_HASH="$hotel_admin_hash" \
    db sh -c 'exec psql \
      -U "$POSTGRES_USER" \
      -d "$POSTGRES_DB" \
      -v ON_ERROR_STOP=1 \
      -v hash_admin="$HOTEL_ADMIN_HASH"' <<'SQL'
BEGIN;

INSERT INTO roles (nom)
VALUES ('ADMIN')
ON CONFLICT (nom) DO NOTHING;

INSERT INTO comptes (nom, email, mot_de_passe, role_id)
SELECT
    'Administrateur',
    'admin@hotel.local',
    :'hash_admin',
    id
FROM roles
WHERE nom = 'ADMIN'
  AND :'hash_admin' ~ '^[$]2[aby][$]12[$][./A-Za-z0-9]{53}$'
ON CONFLICT (email) DO NOTHING;

COMMIT;

SELECT c.email, r.nom AS role
FROM comptes c
JOIN roles r ON r.id = c.role_id
WHERE c.email = 'admin@hotel.local';
SQL
)
```

Résultat attendu : un compte `admin@hotel.local` associé au rôle ADMIN.

Cette opération conserve un compte déjà existant.
Le mot de passe du compte applicatif est distinct du mot de passe
de connexion à PostgreSQL.

## 4. Accéder à l’application

| Interface | Adresse |
|-----------|---------|
| Dashboard | http://localhost:8080/dashboard |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

### Interface web

Se connecter avec le compte administrateur créé à l’étape précédente.
L’interface web utilise une session et la protection CSRF.

### API REST

Dans Swagger UI :

1. Exécuter `POST /api/auth/login` avec l’email et le mot de passe.
2. Copier la valeur du champ `accessToken`.
3. Cliquer sur **Authorize**.
4. Coller le jeton seul dans le champ `bearerAuth`.
5. Confirmer, puis exécuter `GET /api/dashboard`.

Le jeton est valable 15 minutes.
Une requête vers un endpoint protégé sans jeton reçoit une réponse 401.

## 5. Consulter la base PostgreSQL

Ouvrir le client PostgreSQL dans le conteneur :

```bash
docker compose --env-file .local/docker.env exec db \
  sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Les variables de connexion sont résolues dans le conteneur.

Dans le client `psql` :

- `\dt` affiche les tables.
- `\q` ferme le client.

## 6. Arrêter et redémarrer

Arrêter les services :

```bash
docker compose --env-file .local/docker.env down
```

Redémarrer les services :

```bash
docker compose --env-file .local/docker.env up -d
```

Le volume nommé de PostgreSQL conserve les données après ces commandes.
Une nouvelle connexion web est nécessaire après la recréation
du conteneur applicatif.

## 7. Reconstruire après une modification

```bash
mvn clean verify

docker compose --env-file .local/docker.env up -d --build
```

## Dépannage

### Port 8080 occupé ou accès localhost incohérent

Identifier les processus utilisant le port :

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

Arrêter l’ancienne instance du projet depuis son terminal ou Eclipse.

L’adresse IPv4 directe est :

http://127.0.0.1:8080/dashboard

### Consulter les logs

```bash
docker compose --env-file .local/docker.env logs --tail=100 app

docker compose --env-file .local/docker.env logs --tail=100 db
```