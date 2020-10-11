{
	"wiki" : {
		"whatTranscludesHere": [
			{ "title": "Участник:Bot template", "list": "Проект:Project 1/Новые статьи/Параметры" }
		],
		"namespaceIdentifier": [
			{ "number": 0, "id": "" },
			{ "number": 2, "id": "Участник" },
			{ "number": 14, "id": "Категория" }
		],
		"pageText": [
			{
				"title": "Проект:Project 1/Новые статьи/Параметры",
				"text": [
					"{{Участник:Bot template",
					"|тип = новые статьи",
					"|категории = Иваново",
					"|страница = Проект:Project 1/Новые статьи",
					"|формат элемента   = # {{cl|%(название)|%(автор)|%(дата)}}",
					"|элементов = 10",
					"|часов = 4000",
					"|подвал = \\n<noinclude>[[Категория:Википедия:Списки новых статей по темам|{{PAGENAME}}]]</noinclude>",
					"|пространство имён = 14",
					"}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": [
					"# {{cl|Транспортные здания и сооружения Иванова|Wagon|2020-05-21T07:26:19Z}}",
					"# {{cl|Архитектура советского авангарда в Иванове|Мечников|2019-11-24T12:29:31Z}}",
					"# {{cl|Железнодорожные станции и платформы Иванова|Evgeniy Sinin|2019-05-03T14:29:44Z}}",
					"# {{cl|Какая-то хреновая категория|Некто|2019-05-03T14:20:44Z}}",
					"# {{cl|Преподаватели Ивановского художественного училища|Upp75|2018-08-31T08:04:07Z}}",
					"<noinclude>[[Категория:Википедия:Списки новых статей по темам|{{PAGENAME}}]]</noinclude>"
				]
            }
		],
		"exists": [
			"Категория:Транспортные здания и сооружения Иванова",
			"Категория:Архитектура советского авангарда в Иванове",
			"Категория:Железнодорожные станции и платформы Иванова",
			"Категория:Преподаватели Ивановского художественного училища",
		],
		"firstRevision": [
			{
				"revid": 690, "timestamp": 1275804366, "title": "Транспортные здания и сооружения Иванова", "summary": "create page Транспортные здания и сооружения Иванова",
				"user" : "Wagon", "minor": false, "bot": false, "rvnew": true, "size": 500
			}
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	Транспортные_здания_и_сооружения_Иванова	690	Category	277	20200521072619"]
	],
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи",
			"text": [
					"# {{cl|Транспортные здания и сооружения Иванова|Wagon|2020-05-21T07:26:19Z}}",
					"# {{cl|Архитектура советского авангарда в Иванове|Мечников|2019-11-24T12:29:31Z}}",
					"# {{cl|Железнодорожные станции и платформы Иванова|Evgeniy Sinin|2019-05-03T14:29:44Z}}",
					"# {{cl|Преподаватели Ивановского художественного училища|Upp75|2018-08-31T08:04:07Z}}",
					"<noinclude>[[Категория:Википедия:Списки новых статей по темам|{{PAGENAME}}]]</noinclude>"
				],
			"section": -2
		}
	],
	"expected_tools_queries": [
		{
			"contains": ["Иваново"]
		}
	]
}
