# CO2 emission API interface

Application builds an API that contains endpoints from where data about the worst polluters in the world can be found. Live version of the API can be found [here](https://aqueous-mesa-88968.herokuapp.com/api/v1/) - be patient as it can take time for Heroku to serve the app.

Data is read from the csv that is located at `src/main/resources/csv/emissions.csv`. Csv contains data about polluter countries, the year and category of emissions as well as emissions per capita. Original source of the data can be found [here](https://datahub.io/core/co2-fossil-by-nation#data). 

## Example search
For example to find 10 most polluting countries in cement category between years 2000 and 2015, you can write:

<b>[/worst/polluters?from=2000&to=2015&type=cement&top=10](https://aqueous-mesa-88968.herokuapp.com/api/v1/worst/polluters?from=2000&to=2015&type=cement&top=10)</b>

To find 5 most polluting countries in total of all categories from the year 2000 to this date (the newest data currently ends at 2014), use:

<b>[/worst/polluters?from=2011&top=5&type=Total](https://aqueous-mesa-88968.herokuapp.com/api/v1/worst/polluters?from=2011&top=5&type=Total)</b>

Query parameters:
- from: start year of the search, first year there is data is 1751
- to: the end year of the search, last year there is data is 2014
- type: one of these following pollution types - cement, solid, liquid, gasFlaring, gasFuel, perCapita, bunkerFuels or total
- top: how many worst polluters you want to the list - without any top limit search return 0 polluters.

The total type category is the sum of pollution in cement, solid, liquid, gasFlaring and gasFuel categories. Bunker fuels are not taken into account in the total sum.

## Cleaning the data
The data in csv is corrupted and therefore some steps are taken before the data is stored in the database. Logs at `logs/emissions.log` record all the rows that were deleted in the process.
1. Every emission, row of data, must have a named country and a year between 1751 and 2021.
    - 660 rows removed because of either year or country was left empty
    - 1 case where year was marked as "UKRAINE"
2. Quatation marks (" ") were taken into consideration by openCSV. With 4 rows with the country record "BONAIRE, SAINT EUSTATIUS, AND SABA" double quatations were not handled and records were dropped from database.
3. Country "Viet nam" was corrected to English language "Vietnam"
4. Some emissions were not recorded to countries but special events, such as <b>Kuwaiti oil fires</b>. These were left to the database and under Country the name of the event was listed.
5. Around 670 rows had > 0.0000001 difference between the total amount that was listed in the csv and the total amount summed between all the categories (gas fuel, gas flaring, cement, solid and liquid fuels). The amount of cases where this difference was greater than 1 was 11 and in these cases the difference was 2 units (the reported total was always smaller than the sum of categories). The differences are small but since the category numbers were saved to database and total number is calculated from the category amounts as accordance to database normalization rules, these 670 rows total are inflated slightly.
6. Empty emission values were changed to Double.NaN

## Improvement suggestions
1. Data cleaning decisions have been done best to my abilities. However, removing some of the rows may have an impact on the statistic that are shown and therefore further analysis should be done on the rows that are removed. The csv rows are stored in the logs - you can for example see the rows removed due to missing year or country as simply as:
```
cat logs/emissions.log | grep "csv has either no year or no country."
```

