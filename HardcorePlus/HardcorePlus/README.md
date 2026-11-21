# HardcorePlus (Paper 26.x, Java 25)

- Mort = ban de 5 jours réels (modifiable en jeu)
- `/unban <pseudo>` : lève le ban temporaire (et retire de la blacklist si besoin)
- `/unban all` : lève tous les bans temporaires (la blacklist reste intacte)
- `/hardcore` (ou `/hardcor`) : menu admin (durée, sanctions, blacklist)
- Permissions : `hardcore.admin` (OP par défaut), `hardcore.bypass` (immunité à la mort)

## Compiler via GitHub
1. Crée un repo GitHub et envoie tout le contenu de ce dossier.
2. Onglet **Actions** > "Build plugin" : le .jar est dans les *Artifacts* (`HardcorePlus`).
3. Option : `git tag v1.0.0 && git push --tags` pour publier le jar dans les *Releases*.
