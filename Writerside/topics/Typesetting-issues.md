# Typesetting issues

## Nesting of sectioning commands

It is recommended to use proper nesting of `\section`-like commands, for example you should not follow up a `\section` by a `\subsubsection`, but by a `\subsection`.
This inspections includes two quickfixes, to change the sectioning command to the right one (change `\subsubsection` to `\subsection` in this example) and to add the missing sectioning command (`\subsection` in this case).

## Collapse cite commands

`\cite{knuth1990}\cite{goossens1993}` should be replaced with `\cite{knuth1990,goossens1993}`

## En dash in number ranges

Instead of typing `0-9` for a number range, use `0--9` to typeset the right dash.

## Use of `.` instead of `\cdot`

When multiplying numbers, use `\cdot` instead of `.`.

## Use of `x` instead of `\times`

When multiplying numbers, use `2 \times 4` instead of `2x4`.

## Vertically uncentered colon

Instead of `:=`, use `\coloneqq` for better vertical centering of the colon.

## Insert `\qedhere` in trailing displaymath environment

If you end a `proof` environment with a displaymath environment, the qed symbol will appear after the displaymath which is one line too low, so you should use `\qedhere` in the displaymath environment to fix that.

## Dotless versions of i and j must be used with diacritics

When you use diacritics like `\^` on an i or j, you should use the dotless version for better readability.
For example, instead of `\^i` write `\^{\i}`.

## Enclose high commands with `\leftX..\rightX`

Expressions which take up more vertical line space, should also be enclosed with larger parentheses.
For example, instead of `(\frac 1 2)` write `\left(\frac 1 2\right)`.

## Citations must be placed before interpunction

Use `Sentence~\cite{knuth1990}.` and not `Sentence.~\cite{knuth1990}`

## Missing glossary reference

When using a glossary, it is good practice to reference every glossary entry with a \gls-like command.
This makes sure that the list of pages with occurrences in the glossary is complete.
For examples on how to use a glossary, see [External tools](External-tools.md#glossary-examples).