# An attempt at generating poems using Markov Chains

I used poem collections from [Project Gutenberg](https://www.gutenberg.org/) to train both a first order and second order [Markov Chain](https://en.wikipedia.org/wiki/Markov_chain) model. This was done in `Java`. I then extracted the transition probabilities to JSON format for the demo. Using [words](https://github.com/words)'s javascript [rhyming library](https://github.com/words/rhymes), I'm able to determine which words rhyme with the last word on some line, so we can do a depth first search using the transition probabilities from the markov chain model to find a path (a sentence) to a word that rhymes with the last word on some other line.

## Example poem

```
this child of his eyes the
same they said is king so the good
man thought twere pity he should
have kept nigh half the time
of nor ask for has lime
in sight clear as heaven may
man put on defeat as t were a day
and show that this is today
a can experiment is that conquers
might with dian ear make bold
to scorn and as she wills with carpets of gold
and blithe fair face ye are
mad ye have gone acrosst the bar
of seas far away and more
than one close joy i see before
```
## How to use

Demo available at https://marcusgh.github.io/poem-generator/main.html. Click the "Next Line" button to generate a line of poetry.
