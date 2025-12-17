# PAMGuard Goniometer Module

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.17965495.svg)](https://doi.org/10.5281/zenodo.17965495)

PAMGUard plugin module to interpret and display Fastoc GPS data from a Lotek F6G149A GPS-Argos unit (as part of a suction-cup attached Mixed-Dtag++).

The tag records FastGPS positions and relays them to ARGOS. However, for real time tracking, delivery of data to a tracking vessel via ARGOS can be too slow, so the 
[Goniometer](https://www.argos-system.org/products/platform-finder-goniometer/) is used to intercept the signals in real time from the tracking vessel. 
FastGPS Realtime Software from Lotek then combines the Fastloc data extracted from the ARGOS signal with ephemeris data obtained via a UBlox GPS to calculate
GPS locations for the tagged animal(s) in near real time. 

This PAMGuard module helps to setup and configure the Lotec software, then collects output from that software to bring the GPS positions into PAMGuard where they 
are stored in the PAMGuard database and made available for display on the PAMGuard map. 
