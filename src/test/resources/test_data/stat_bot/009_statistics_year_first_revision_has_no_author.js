{
	"system_time": "2021-02-20T21:00:00Z",
	"wiki" : {
		"whatTranscludesHere": [
			{ "title": "Участник:NirvanaBot/Параметры статистики", "list": "Проект:Project 1/Новые статьи/Статистика/Параметры" }
		],
		"namespaceIdentifier": [
			{ "number": 0, "id": "" },
			{ "number": 2, "id": "Участник" }
		],
		"pageText": [
			{
				"title": "Проект:Project 1/Новые статьи/Статистика/Параметры",
				"text": [
					"{{Участник:NirvanaBot/Параметры статистики",
					"	|архив = Проект:Project 1/Новые статьи/Архив",
					"	|параметры архива = снизу",
					"	|первый год = 2020",
					"	|задание = статистика",
					"	|комментарий = обновление статистики для портала",
					"	|сортировать = да",
					"	|кэш = нет",
					"	|только кэш = нет",
					"	|тип = по годам",
					"	|по годам = Проект:Project 1/Новые статьи/Статистика/По годам",
					"}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи/Статистика/По годам",
                "text": null
            },
			{
				"title": "Проект:Project 1/Новые статьи/Архив",
				"text": [
				"* [[Бамидбар рабба]]",
				"* [[Ледниковый медведь]]",
				"* [[Гидрогеназа]]"
				]
			}
		],
		"firstRevision": [
			{
				"revid": 111409625, "timestamp": "2020-12-30T15:33:28Z", "title": "Гидрогеназа", "summary": "начал, частично переводом статьи [[:en:Hydrogenase]]",
				"user" : "Thinknot", "minor": false, "bot": false, "rvnew": true, "size": 6196
			}, {
				"revid": 111409825, "timestamp": "2020-12-30T15:44:42Z", "title": "Ледниковый медведь", "summary": "[[ВП:←|←]] Новая страница: «'''Ледниковый медведь'''",
				"user" : null, "minor": false, "bot": false, "rvnew": true, "size": 1941
			}, {
				"revid": 111413762, "timestamp": "2020-12-30T20:11:15Z", "title": "Бамидбар рабба", "summary": "трактат на кн. чисел",
				"user" : null, "minor": false, "bot": false, "rvnew": true, "size": 1283
			}
		],
		"resolveRedirect": [
			{
				"title": "Ледниковый медведь",
				"resolvedTitle": null
            },
			{
				"title": "Бамидбар рабба",
				"resolvedTitle": "Бемидбар рабба"
			}
		],
		"pageHistory": [
		    {
				"title": "Ледниковый медведь", "start_time": 1609343082000, "end_time": 1672415082000, "revisions": [
					{
						"revid": 111409826, "timestamp": "2020-12-31T15:44:42Z", "title": "Ледниковый медведь", "summary": "some change",
						"user" : "Annie.losenkova", "minor": false, "bot": false, "rvnew": false, "size": 2000, "previous": 111409825
					}
				]
			},
			{
				"title": "Бемидбар рабба", "start_time": 1609359075000, "end_time": 1672431075000, "revisions": [
					{
						"revid": 111413763, "timestamp": "2020-12-31T20:11:15Z", "title": "Бемидбар рабба", "summary": "some change",
						"user" : "Marimarina", "minor": false, "bot": false, "rvnew": false, "size": 1583, "previous": 111413762
					}
				]
			}
		]
	},
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи/Статистика/По годам",
			"text": [
"{| class=\"wikitable sortable\" style=\"text-align:center\"",
"|-",
"! № !! год !! статей !! прогресс !! среднее за день !! MAX за день !! самый активный",
"|-",
"| 1 || [[|2020]] || 3 || {{нет изменений}} || 0 || 2 || {{u|Thinknot}} (1)"
"|-",
"! итого !! !! 3 !! !! 0 !! 2 !! {{u|Thinknot}} (1)",
"|}"
			],
			"section": -2
		}
	]
}