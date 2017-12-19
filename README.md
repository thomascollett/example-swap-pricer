# Swap Pricer

A simple example of a swap pricer using the Strata calculation runner. 100k fixed-float 30Y USD LIBOR swap trades are
generated, and priced against curves calibrated against a static set of quotes.

This project is aimed at being used for performance testing as part of a distributed system, so the calculations are
executed on a single (the direct) thread.

To introduce other scenarios, the quotes can be perturbed and the curve re-calibrated prior to each pricing run.
