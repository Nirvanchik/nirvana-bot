{
	"wiki" : {
		"whatTranscludesHere": [
			{ "title": "Участник:Bot template", "list": "Проект:Project 1/Новые статьи/Параметры" }
		],
		"namespaceIdentifier": [
			{ "number": 0, "id": "" },
			{ "number": 2, "id": "Участник" }
		],
		"pageText": [
			{
				"title": "Проект:Project 1/Новые статьи/Параметры",
				"text": [
					"{{Участник:Bot template",
					"|тип             = новые статьи",
					"|категории       = Палеонтология",
					"|игнорировать    = Палеогеография, Недавно вымершие виды, Динозавры в фантастике",
					"|страница = Проект:Project 1/Новые статьи",
					"|формат элемента = # [[%(название)]]",
					"|шаблоны         = Хорошая статья, Избранная статья, Статья года",
					"|глубина         = 10",
					"}}"]
            },
            {
                "title": "Проект:Project 1/Новые статьи",
                "text": null
            }
		],
		"firstRevision": [
			{	
				"revid": 666, "timestamp": 1275804366000, "title": "Lavanify miolaka", "summary": "create page Lavanify_miolaka",
				"user" : "MegaUser 1", "minor": false, "bot": false, "rvnew": true, "size": 500
			}, {
				"revid": 680, "timestamp": 1275804400000, "title": "Альбертозавр", "summary": "create new fucking page",
				"user" : "MegaUser 2", "minor": false, "bot": false, "rvnew": true, "size": 300
			}, {
				"revid": 685, "timestamp": 1275804500000, "title": "Анкилозавр", "summary": "create new page",
				"user" : "MegaUser 3", "minor": false, "bot": false, "rvnew": true, "size": 666
			}, {
				"revid": 686, "timestamp": 1275805366000, "title": "Браун, Барнум", "summary": "create new page",
				"user" : "MegaUser 4", "minor": false, "bot": false, "rvnew": true, "size": 700
			}, {
				"revid": 687, "timestamp": 1436837856000, "title": "Гипсилофодон", "summary": "create new page",
				"user" : "Lemmy Indarkness", "minor": false, "bot": false, "rvnew": true, "size": 1000
			}, {
				"revid": 688, "timestamp": 1436837810000, "title": "Гулд, Стивен Джей", "summary": "create new page",
				"user" : "MegaUser 6", "minor": false, "bot": false, "rvnew": true, "size": 1100
			}
		],
		"topRevision": [
			{
				"title": "Проект:Project 1/Новые статьи"
			} 
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	Lavanify_miolaka	4707396		26927	20160613105807",
		"2	Альбертозавр	1023865		64149	20160613105807",
		"3	Анкилозавр	1009928		39118	20160613105807",
		"4	Браун,_Барнум	332666		112310	20160625163611",
		"5	Гипсилофодон	274122		23846	20160613105807",
		"6	Гулд,_Стивен_Джей	99243		50167	20160625171629"
		]
	],
	"expected_edits": [
		{
			"title": "Проект:Project 1/Новые статьи",
			"text": [
				"# [[Lavanify miolaka]]",
				"# [[Альбертозавр]]",
				"# [[Анкилозавр]]",
				"# [[Браун, Барнум]]",
				"# [[Гипсилофодон]]",
				"# [[Гулд, Стивен Джей]]"
				],
			"section": -2
		}
	],
	"expected_tools_queries": [
		{
			"contains": ["Палеонтология", "Палеогеография", "Недавно вымершие виды", "Динозавры в фантастике", "Хорошая статья", "Избранная статья", "Статья года"]
		}
	]
}