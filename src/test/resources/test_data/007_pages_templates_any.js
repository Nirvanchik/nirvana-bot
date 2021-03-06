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
					"|тип             = статьи с шаблонами",
					"|категории       = Палеонтология",
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
		]
	},
	"wiki_tools" : [[
		"number	title	pageid	namespace	length	touched",
		"1	Lavanify_miolaka	4707396		26927	20160613105807",
		"2	Альбертозавр	1023865		64149	20160613105807",
		"3	Анкилозавр	1669928		39118	20160613105807",
		"4	Браун,_Барнум	2132666		112310	20160625163611",
		"5	Гипсилофодон	3274122		23846	20160613105807",
		"6	Гулд,_Стивен_Джей	699243		50167	20160625171629",
		"7	Нигерзавр	3259687		44321	20160614112412",
		"8	Патагозавр	4719699		29621	20160613105807",
		"9	Протоптер	2490252		80735	20160624132152",
		"10	Тарбозавр	1388287		68383	20160613105807"
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
				"# [[Гулд, Стивен Джей]]",
				"# [[Нигерзавр]]",
				"# [[Патагозавр]]",
				"# [[Протоптер]]",
				"# [[Тарбозавр]]"
				],
			"section": -2
		}
	],
	"expected_tools_queries": [
		{
			"contains": ["Палеонтология", "Хорошая статья", "Избранная статья", "Статья года", "templates_any"]
		}
	]
}