{
	"wiki" : {
		"whatTranscludesHere": [
			{ "title": "Участник:Bot template", "list": "Проект:Project 1/Новые статьи/Параметры" }
		],
		"namespaceIdentifier": [
			{ "number": 0, "id": "" },
			{ "number": 2, "id": "Участник" }
			{ "number": 10, "id": "Шаблон" }
		],
		"pageText": [
			{
				"title": "Проект:Project 1/Новые статьи/Параметры",
				"text": [
					"{{Участник:Bot template",
					"|тип = новые статьи",
					"|категории = Метал-группы России, Метал-группы Белоруссии, Метал-группы Украины",
					"|страница = Проект:Project 1/Новые статьи",
					"|элементов = 10",
					"|часов = 8760",
					"|глубина = 3",
					"|формат элемента = {{Новая статья|%(название)|%(дата)|%(автор)}}",
					"|шаблоны с параметром = Просто шаблон, Музыкальный коллектив/Язык/Русский язык, Музыкальный коллектив/Язык/русский язык",
					"}}"]
			},
			{
				"title": "Тень солнца",
				"text": [
				"Начало статьи.",
				"{{Просто шаблон|какие-то данные, неважно какие}}",
				"",
				"'''Тень солнца''' — раз-два-три, локаторы протри."
				]
			},
			{
				"title": "Scream In Darkness",
				"text": [
				"{{Музыкальный коллектив",
				"| Название         = Scream In Darkness",
				"| Лого             = SIDlogo600.png",
 				"| Фото             = ",
				"| Описание_фото    = Логотип Scream In Darkness",
				"| Ширина_фото      = 300px",
				"| Годы             = 2004 — наши дни",
				"| Страны           = {{RUS}}",
				"| Город            = {{Флаг|Москва}} [[Москва]]",
				"| Жанры            = [[Мелодичный дэт-метал]]",
				"| Язык             = [[русский язык|русский]]",
				"| Лейбл            = [[Irond Records]] (2009-2012)",
				"| Состав           = Макс Крюков - гитара, вокал<br />Головко Юрий - бас, вокал<br />Игорь Сорокин - барабаны",
				"| Другие проекты   = [http://infernalcry.ru/ Infernal Cry]",
				"| Сайт             = [http://indarkness.ru официальный сайт]",
				"}}",
				"",
				"'''«Scream In Darkness»''' - российский [[музыкальный коллектив]], основанный в [[2004]] году в [[Москва|Москве]] и исполняющий музыку в жанре [[мелодичный дэт-метал]]. "
				]
			},
			{
				"title": "Tacit Fury",
				"text": [""]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": null
            }
		],
		"firstRevision": [
			{
				"revid": 666, "timestamp": 1275804366000, "title": "Тень солнца", "summary": "create page Квазисобака",
				"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 680, "timestamp": 1275804400000, "title": "Слово (альбом)", "summary": "create new fucking page",
				"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			}, {
				"revid": 685, "timestamp": 1275804500000, "title": "Tacit Fury", "summary": "create new page",
				"user" : "MegaUser 3", "minor": false, "bot": false, "rvnew": true, "size": 666
			}, {
				"revid": 686, "timestamp": 1275805366000, "title": "MULTIVERSE", "summary": "create new page",
				"user" : "MegaUser 4", "minor": false, "bot": false, "rvnew": true, "size": 700
			}, {
				"revid": 687, "timestamp": 1436837856000, "title": "Scream In Darkness", "summary": "create new page",
				"user" : "Lemmy Indarkness", "minor": false, "bot": false, "rvnew": true, "size": 1000
			}
		],
		"templates": [
			{
				"title": "Scream In Darkness",
				"templates": ["Шаблон:Музыкальный коллектив"]
			},
			{
				"title": "Тень солнца",
				"templates": ["Шаблон:Просто шаблон"]
			},
			{
				"title": "Tacit Fury",
				"templates": ["Шаблон:Какой-то шаблон"]
			},
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	Тень_солнца	4350785		35169	20160625015051",
		"2	Слово_(альбом)	6149797		6559	20160623100936",
		"3	Tacit_Fury	6082689		12205	20160623100936",
		"4	MULTIVERSE	6003741		6518	20160624224342",
		"5	Scream_In_Darkness	5882971		14091	20160623100936"]
	],
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи",
			"text": [
					"{{Новая статья|Scream In Darkness|2015-07-14T01:37:36Z|Lemmy Indarkness}}",
					"{{Новая статья|Тень солнца|2010-06-06T06:06:06Z|MegaUser 1}}"
				],
			"section": -2
		}
	],
	"expected_tools_queries": [
		{
			"contains": ["Метал-группы России", "Метал-группы Белоруссии", "Метал-группы Украины"]
		}
	]
}