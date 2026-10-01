---
name: rogue-writing-lector
description: Independently review the language, tone, voice, and style of Rogue Commander creative writing after it is created or revised in repository files. Use when the current request primarily writes or edits story, dialogue, lore, narrative, NPC interactions, character text, or flavor text. Do not use for code-only work, discussion without creative-text changes, or solely because an earlier request involved creative writing.
---

# Rogue Writing Lector

Apply this workflow only to creative text changed for the current request.

1. Before writing, follow the Story and Lore Writing rules in `AGENTS.md`.
   Read `story/story.md` and the relevant story or character guides it routes
   to, including the applicable named-NPC rubric when needed.
2. Create or revise the requested text within the user's scope.
3. If no repository file containing creative text changed, do not run the
   lector review.
4. After the creative edits, delegate an independent lector subagent. Give it
   the current request, the changed creative text or diff, and the applicable
   guides. Instruct it to review without editing files.
5. Treat the current request, `AGENTS.md`, `story/story.md`, and the applicable
   story or character guides as the complete basis for review.
6. The lector's primary mandate is editorial. Assess how the changed prose
   reads and whether its language, tone, voice, and style follow the applicable
   writing rules in the review basis. Do not create a separate style checklist
   that restates or expands those rules.
7. Review continuity, lore facts and character facts only secondary and not as a replacement for language-and-tone review.
8. Do not introduce extra review standards based on personal taste, realism,
   plausibility, or physical correctness. Do not spend the report proving that
   the text is logically supported or inventorying everything it gets right.
   If no actionable language or tone concern remains, say so briefly.
9. The lector is an editorial reviewer, not a code reviewer. Do not use bug or
   severity labels such as P0, P1, P2, or P3, and do not frame prose findings as
   software defects. Use file and line
   references only to locate the relevant passage.
10. The lector diagnoses; it does not rewrite. Do not provide drop-in replacement
   prose or use instructions such as "replace with" unless the user explicitly
   asks the lector to draft alternatives.
11. The original agent evaluates the findings and remains the writer. It decides
   which findings are valid, creates any necessary revisions in its own words,
   and must not copy lector phrasing mechanically. Apply only corrections that
   are supported and remain within the requested scope, then inspect the final
   creative-text diff before responding.

If independent subagents are unavailable, state that the independent review
could not be performed; do not claim that it occurred.
