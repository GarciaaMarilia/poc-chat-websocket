# PoC — Chat temps réel · Your Car Your Way

Preuve de concept de la fonctionnalité de chat en temps réel entre un client et le service client, conformément aux spécifications de l'ADD.

## Stack technique

| Couche               | Technologie                     | Référence ADD |
| -------------------- | ------------------------------- | ------------- |
| Frontend             | HTML + JavaScript (page unique) | Section 5.3.1 |
| Backend              | Spring Boot 3 / Java 21         | Section 5.3.2 |
| Protocole temps réel | WebSocket (STOMP over SockJS)   | Section 5.1.5 |
| Librairies client    | StompJS 7 + SockJS 1 (via CDN)  | Section 5.3.2 |

> Le frontend est une page HTML autonome. Aucune installation npm n'est requise.
> Les librairies SockJS et StompJS sont chargées directement via CDN.

---

## Architecture du PoC

```
Frontend HTML (port 4200 — servi par Python)
    └── StompJs.Client
            │  WebSocket / SockJS
            ▼
Spring Boot (port 8080)
    └── WebSocketConfig  →  /ws-chat  (endpoint SockJS)
    └── ChatController
            │  /app/chat.send   (réception messages)
            │  /app/chat.join   (notification connexion)
            ▼
        SimpleBroker  →  /topic/chat  (diffusion à tous les abonnés)
```

---

## Lancer le projet

### Prérequis

- Java 21 installé
- Maven installé
- Python 3 installé (pour servir le frontend)

### 1. Démarrer le backend

```bash
cd backend
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run
```

Le serveur démarre sur `http://localhost:8080`.

### 2. Servir le frontend

Dans un second terminal :

```bash
cd frontend
python3 -m http.server 4200
```

### 3. Ouvrir dans le navigateur

```
http://localhost:4200
```

Pour tester le chat entre deux utilisateurs, ouvrir **deux onglets** avec la même URL et se connecter avec des prénoms différents.

### Tests backend

```bash
cd backend
mvn test
```

---

## Structure du projet

```
poc-chat/
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/yourcaryourway/chat/
│       │   ├── ChatApplication.java
│       │   ├── config/
│       │   │   └── WebSocketConfig.java      ← config STOMP + SockJS
│       │   ├── controller/
│       │   │   └── ChatController.java       ← handlers WebSocket
│       │   ├── model/
│       │   │   └── ChatMessage.java          ← entité message
│       │   └── service/
│       │       └── ChatService.java          ← logique métier
│       └── test/.../service/
│           └── ChatServiceTest.java
└── frontend/
    └── index.html                            ← page unique, aucune dépendance
```

---

## Ce que ce PoC démontre

- Connexion WebSocket persistante via STOMP over SockJS
- Diffusion temps réel des messages à tous les abonnés (`/topic/chat`)
- Reconnexion automatique en cas de coupure réseau (`reconnectDelay: 5000ms`)
- Séparation claire `USER` / `AGENT` dans les messages
- Validation des entrées côté serveur (`@NotBlank`, `@Size`)
- Messages propres affichés à droite, messages des autres à gauche

---

## Ce qui est hors périmètre du PoC

Conformément aux recommandations, les éléments suivants ne sont **pas** implémentés dans ce PoC et feront partie de l'implémentation complète :

- Authentification JWT
- Persistance des messages en base de données (entités `SupportTicket`, `SupportMessage`)
- Identification du rôle agent vs client via token
- Historique des conversations
- Notifications push par e-mail
