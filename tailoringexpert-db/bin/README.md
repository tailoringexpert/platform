# tailoringexpert-db

Module that provides liquibase scripts to create and update all database instances.
Each database must first run all the platform db scripts and then the dependent scripts for its master data.

## Queries

### MySQL

#### Select a requirement of a dedicated project tailoring

See [Create hierarchical MySQL queries using WITH RECURSIVE syntax](https://sql-query.dev/articles/403).

``` sql
WITH RECURSIVE
tr AS (
    SELECT CHAPTER_ID, PARENTCHAPTER_ID, NAME 
  	    FROM TAILORINGCATALOGCHAPTER 
  	    WHERE CHAPTER_ID = (
  	        SELECT CHAPTER_ID 
  	            FROM TAILORINGCATALOG 
  	            WHERE CATALOG_ID = (
  	                SELECT TAILORINGCATALOG_ID 
  	                    FROM TAILORING 
  	                    WHERE NAME='$TAILORINGNAME' AND PROJECT_ID = (
  	                        SELECT PROJECT_ID 
  	                            FROM PROJECT 
  	                            WHERE IDENTIFIER='$IDENTIFIER'
  	                    )
  	            )
        )
    UNION ALL
    SELECT t.CHAPTER_ID, t.PARENTCHAPTER_ID, t.NAME 
  	    FROM TAILORINGCATALOGCHAPTER t
  	    JOIN tr ON t.PARENTCHAPTER_ID = tr.CHAPTER_ID
)

Select * from TAILORINGREQUIREMENT where NUMBER='$NUMBER' and CHAPTER_ID in (select CHAPTER_ID from tr)
```

#### Select a requirement of a dedicated base catalog
``` sql
WITH RECURSIVE
br AS (
    SELECT CHAPTER_ID, PARENTCHAPTER_ID, NAME
        FROM BASECATALOGCHAPTER
        WHERE CHAPTER_ID = (
            SELECT CHAPTER_ID
                FROM BASECATALOG
                WHERE VERSION = '8.2.1'
        )
    UNION ALL
    SELECT b.CHAPTER_ID, b.PARENTCHAPTER_ID, b.NAME
        FROM BASECATALOGCHAPTER b
        JOIN br ON b.PARENTCHAPTER_ID = br.CHAPTER_ID
)
Select * from BASEREQUIREMENT where NUMBER='$NUMBER' and CHAPTER_ID in (select CHAPTER_ID from br)
```
