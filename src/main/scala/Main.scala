// package checkers

// val jsonTest = """
// [
//   {
//     "name": "Flossie Shaffer",
//     "age": 26,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 60
//       },
//       {
//         "label": "DEX",
//         "level": 19
//       },
//       {
//         "label": "INT",
//         "level": 88
//       },
//       {
//         "label": "CON",
//         "level": 98
//       }
//     ]
//   },
//   {
//     "name": "Jefferson Maynard",
//     "age": 27,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 22
//       },
//       {
//         "label": "DEX",
//         "level": 14
//       },
//       {
//         "label": "INT",
//         "level": 96
//       },
//       {
//         "label": "CON",
//         "level": 77
//       }
//     ]
//   },
//   {
//     "name": "Karyn Lindsey",
//     "age": 29,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 50
//       },
//       {
//         "label": "DEX",
//         "level": 84
//       },
//       {
//         "label": "INT",
//         "level": 22
//       },
//       {
//         "label": "CON",
//         "level": 63
//       }
//     ]
//   },
//   {
//     "name": "Holt English",
//     "age": 21,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 26
//       },
//       {
//         "label": "DEX",
//         "level": 92
//       },
//       {
//         "label": "INT",
//         "level": 74
//       },
//       {
//         "label": "CON",
//         "level": 77
//       }
//     ]
//   },
//   {
//     "name": "Carlene Colon-",
//     "age": -32,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 40
//       },
//       {
//         "label": "DEX",
//         "level": 80
//       },
//       {
//         "label": "INT",
//         "level": 26
//       },
//       {
//         "label": "CON+",
//         "level": 12
//       }
//     ]
//   },
//   {
//     "name": "Tami Stout",
//     "age": 37,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STRs",
//         "level": 58
//       },
//       {
//         "label": "DEXs",
//         "level": 44
//       },
//       {
//         "label": "INT",
//         "level": 65
//       },
//       {
//         "label": "CON",
//         "level": 91
//       }
//     ]
//   },
//   {
//     "name": "Violet Golden",
//     "age": 25,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 62
//       },
//       {
//         "label": "DEX",
//         "level": 45
//       },
//       {
//         "label": "INT",
//         "level": 36
//       },
//       {
//         "label": "CON",
//         "level": 66
//       }
//     ]
//   },
//   {
//     "name": "Karina Cain",
//     "age": 36,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 21
//       },
//       {
//         "label": "DEX",
//         "level": 36
//       },
//       {
//         "label": "INT",
//         "level": 27
//       },
//       {
//         "label": "CON",
//         "level": 98
//       }
//     ]
//   },
//   {
//     "name": "Bean Morton",
//     "age": 40,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 82
//       },
//       {
//         "label": "DEX",
//         "level": 93
//       },
//       {
//         "label": "INT",
//         "level": 85
//       },
//       {
//         "label": "CON",
//         "level": 76
//       }
//     ]
//   },
//   {
//     "name": "Rosalie Wong",
//     "age": 21,
//     "gender": {
//       "Male": {}
//     },
//     "perks": [
//       {
//         "label": "STR",
//         "level": 65
//       },
//       {
//         "label": "DEX",
//         "level": 18
//       },
//       {
//         "label": "INT",
//         "level": 14
//       },
//       {
//         "label": "CON",
//         "level": 40
//       }
//     ]
//   }
// ]

// """