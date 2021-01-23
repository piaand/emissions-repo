# CO2 emission API interface

Application builds an API that contains endpoints from where data about the worst polluters in the world can be found. Live version of the API can be found [here](https://aqueous-mesa-88968.herokuapp.com/api/v1/) - be patient as it can take time for Heroku to serve the app.

Data is read from the csv that is located at `src/main/resources/csv/emissions.csv`. Csv contains data about polluter countries, the year and category of emissions as well as emissions per capita. Original source of the data can be found [here](https://datahub.io/core/co2-fossil-by-nation#data). 

## Cleaning the data
The data in csv is corrupted and therefore some steps are taken before the data is stored in the database. Logs at `logs/emissions.log` record all the rows that were deleted in the process.
1. Every emission, row of data, must have a named country and a year between 1751 and 2021.
    - 660 rows removed because of either year or country was left empty
    - 1 case where year was marked as "UKRAINE"
2. Quatation marks (" ") from country names and years were removed
3. Country "Viet nam" was corrected to English language "Vietnam"
4. Some emissions were not recorded to countries but special events, such as <b>Kuwaiti oil fires</b>. These were left to the database and under Country the name of the event was listed.

## Improvement suggestions
1. Data cleaning decisions have been done best to my abilities. However, removing some of the rows may have an impact on the statistic that are shown and therefore further analysis should be done on the rows that are removed. The csv rows are stored in the logs - you can for example see the rows removed due to missing year or country as simply as:
```
cat logs/emissions.log | grep "csv has either no year or no country."
```