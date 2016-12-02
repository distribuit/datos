# Datos
Data movement simplified  



### Motivation
In modern enterprise application its quite common for many sub systems like realtime-system, batch-system, anomaly-detection-system, auditing-system etc to process the input in parallel from a common source. This is a problem space in self when dimensions like processing atomicity, transactions and no single point of failure becomes priority. datos addresses this problem by decoupling input source system from the subsystems and there by provides in-memory data translations and data compression. This advancement will make the subsystems efficient and support realtime event handling practical.  



### Get started

1. Clone this repository.

2. Configure this file   [Workers.json] (https://github.com/distribuitech/datos/blob/development/datos/settings/Workers.json)  
       Notes: 
       * Path follows [common-vfs](http://commons.apache.org/proper/commons-vfs/filesystems.html) naming pattern  
       * Supported [compression](https://github.com/distribuitech/datos/blob/development/datos/src/main/scala/com/distribuit/datos/compression/Compression.scala) formats are  GZIP,BZIP2,ZIP,XZ,DEFLATE and PACK200.  
              
3. Run [assembly](https://github.com/distribuitech/datos/blob/development/datos/sbin/assembly) script.  
4. Start Datos with [datos](https://github.com/distribuitech/datos/blob/development/datos/bin/datos) script.  
