# Project rules

См. [ARCHITECTURE.md](ARCHITECTURE.md) — правила архитектуры фичей.

- Before every commit, remove imports that are unused in the code (mind implicit usage, e.g. getValue/setValue for `by` delegates).

Workflow:
1. The owner gives a task.
2. I build a plan and wait for "ок" or plan corrections.
3. I implement the task and propose a short English commit message.
4. The owner confirms or changes the commit message — only then commit.

Exception: if the owner asks to commit right away ("закоммить сразу" / "commit it now"), step 3 becomes: implement and commit immediately with a short English message, no proposal.
