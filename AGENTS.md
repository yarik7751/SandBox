# Project rules

См. [ARCHITECTURE.md](ARCHITECTURE.md) — правила архитектуры фичей.

- Before every commit, remove imports that are unused in the code (mind implicit usage, e.g. getValue/setValue for `by` delegates).
- После каждого изменения кода запускать `./gradlew detekt` и чинить все находки в новом коде до предложения коммита. Baseline не переписывать — старые замороженные находки не трогаем. (Для правок без кода — только текст, конфиги, md — прогон не нужен.)

Workflow:
1. The owner gives a task.
2. I build a plan and wait for confirmation "ок", "ОК" or "ok" (or plan corrections). Никакой код не пишется до подтверждения.
   - План включает примеры будущих изменений в коде: фрагменты кода, сигнатуры функций/интерфейсов, структуру новых файлов — то, что помогает оценить план до реализации. Это эскиз, а не финальный код.
3. I implement the task and run `./gradlew detekt`:
   - detekt зелёный → предлагаю короткий commit message на английском;
   - detekt красный → чиню находки в написанном коде, прогоняю снова.
4. The owner confirms or changes the commit message — only then commit.

Exception: if the owner asks to commit right away ("закоммить сразу" / "commit it now"), step 3 becomes: implement and commit immediately with a short English message, no proposal.
