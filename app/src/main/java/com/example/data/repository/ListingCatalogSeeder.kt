package com.example.data.repository

import com.example.data.model.ListingItem
import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.utils.LinkMetadataFetcher

object ListingCatalogSeeder {

    fun generateFullCatalog(): List<ListingItem> {
        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

        val categoriesData = listOf(
            CategorySeedData(
                category = "News",
                groups = listOf(
                    "Global Breaking News 24/7 📰" to "Real-time international headlines, politics, weather, and breaking updates from verified sources.",
                    "Daily Hindi & Urdu Headlines 🗞️" to "Subah ki taaza khabrein, desh-videsh ki mukhy samachar aur viral public updates.",
                    "Live Weather Radar & City Alerts ⚡" to "Storm tracking, monsoon rainfall forecasts, temperature alerts, and climate bulletins.",
                    "National Political Debate & Analysis 🏛️" to "Unbiased political debate, election updates, parliament bills, and expert interviews.",
                    "BBC & CNN Live Bulletin Hub 🌍" to "Round the clock global breaking news alerts, documentaries, and press briefings.",
                    "Asia Defense & Military Pulse 🛡️" to "Geopolitical security discussions, defense deals, border news, and army updates.",
                    "Metro Traffic & Local City News 🚦" to "Live daily traffic rush alerts, city transport updates, and local municipal notices.",
                    "Gulf & Middle East Urdu News 🌙" to "UAE, Saudi Arabia, Qatar job laws, flight schedules, and Middle East developments.",
                    "Tech & Science Headlines Daily 🔬" to "Space missions, AI inventions, gadgets launches, and scientific discoveries.",
                    "Daily Express & Jang Khabrain 📰" to "Front-page newspaper clips, editorials, columns, and national breaking scoops.",
                    "Financial & Stock Market News 📈" to "Stock exchange closing, inflation rates, central bank rates, and currency values.",
                    "Crime Investigation & Legal Reports ⚖️" to "Court verdicts, supreme court rulings, cyber law alerts, and consumer rights.",
                    "Pak-India Peace & Cultural Forum 🤝" to "Cross-border cultural discussions, heritage journalism, and friendship stories.",
                    "Health, Pharma & Medical Alerts 🏥" to "Medical research, pandemic alerts, medicine shortages, and wellness reports.",
                    "Environmental Watch & Green Earth 🌿" to "Climate change awareness, pollution indices, tree planting, and eco news.",
                    "Global Fact Check & Verified News 🔍" to "Debunking fake news, deepfakes, viral social rumors, and verified facts."
                ),
                channels = listOf(
                    "World Geopolitics & Defense News 🌐" to "Official channel for deep analysis of military, economy, and foreign affairs.",
                    "Global Tech Disruption Channel ⚡" to "Silicon Valley scoops, AI breakthroughs, and hardware launch streams.",
                    "Daily Urdu Express Official Channel 🗞️" to "Verified live breaking newsfeed, instant headlines, and editorial columns.",
                    "Live City Alert & Emergency Desk 🚨" to "Instant broadcast of weather emergencies, transit strikes, and civil advisories.",
                    "International Diplomatic Wire 🏛️" to "UN briefings, summit agreements, trade treaties, and diplomatic press notes.",
                    "Business Standard Live Feed 📊" to "Real-time market indices, crude oil, precious metals, and economic forecasts.",
                    "Defense Strategic Forum Channel 🛰️" to "Aviation radar, strategic weapons analysis, and regional defense monitoring.",
                    "World Sports News Bulletin ⚽" to "FIFA, ICC, Olympic live scorecards, press conferences, and transfer windows.",
                    "FactCheck Global Wire ✔️" to "Instant verification of trending media rumors, official government rebuttals.",
                    "Middle East Gulf Digest 🌴" to "Official labor guidelines, visa regulations, and regional Arab world news.",
                    "Aviation & Aerospace News ✈️" to "Airlines routes, aircraft orders, safety bulletins, and airport expansions.",
                    "Education & Campus News Feed 🎓" to "University admissions, scholarship deadlines, and global academic rankings.",
                    "Public Health & Medical Channel 💊" to "WHO guidelines, vaccination advisories, and certified doctor podcasts.",
                    "Automobile & EV Industry News 🚗" to "Electric vehicle trends, battery tech, and automotive trade developments.",
                    "Space Exploration & NASA Feed 🚀" to "Rocket launches, lunar landings, Mars rovers, and astronomy picture streams.",
                    "Global Law & Human Rights Digest 📜" to "International court decisions, human rights monitoring, and legal reforms."
                )
            ),
            CategorySeedData(
                category = "Entertainment",
                groups = listOf(
                    "Bollywood & Hollywood Mania 🌟" to "First-day box office collections, upcoming teaser trailers, and celebrity interviews.",
                    "OTT Web Series & Netflix Binge 🍿" to "Best binge-worthy series recommendations, IMDb top ratings, and hidden indie film gems.",
                    "Music Lovers & Acoustic Chill 🎸" to "Coke Studio tracks, acoustic covers, Lo-Fi beats, and guitar chord sheets.",
                    "K-Drama & Anime Universe 🌸" to "Anime seasonal releases, manga discussions, K-pop idols, and streaming links.",
                    "Celebrity Gossip & Paparazzi Snaps 📸" to "Red carpet fashion, celebrity weddings, airport looks, and exclusive gossip.",
                    "Filmmakers & Scriptwriters Hub 🎬" to "Short film screenplay critiques, cinematography tips, and editing workflows.",
                    "Standup Comedy & Roast Central 😂" to "Upcoming standup comedy tour dates, open mic registrations, and funny reels.",
                    "Theater, Broadway & Drama Club 🎭" to "Stage plays, acting workshops, classical dramatics, and performance arts.",
                    "Rock & Metal Underground Club 🥁" to "Heavy metal riffs, independent indie bands, album reviews, and concert tours.",
                    "EDM & DJ Remixes Community 🎧" to "Festival line-ups, electronic beats, synthesizer drops, and DJ set recordings.",
                    "Classic 90s Nostalgia Cinema 📼" to "Golden era songs, vintage cinema posters, and timeless retro memories.",
                    "Gaming Streamers & Esports Fans 🎮" to "Twitch watch parties, streamer clips, esports championships, and giveaways.",
                    "Song Lyrics & Poetry Melodies 🎶" to "Soulful songwriting, vocal covers, acoustic jam sessions, and lyric breakdown.",
                    "Dubbing & Voiceover Artists Guild 🎙️" to "Anime dubbing, commercial voiceover tips, accent training, and casting calls.",
                    "Animation & VFX Creators Circle 🖥️" to "Blender 3D tips, Unreal Engine cinematics, CGI breakdowns, and animation reels.",
                    "Cinema Quiz & Trivia Champions 🏆" to "Daily movie quizzes, trivia battles, poster guessing games, and prizes."
                ),
                channels = listOf(
                    "Movie Trailers & Cinema Hub 🎬" to "Hollywood and Bollywood HD trailers, release dates, and theatrical posters.",
                    "Coke Studio & Acoustic Originals 🎵" to "Official audio tracks, behind-the-scenes recordings, and artist interviews.",
                    "Anime Streaming & Manga Updates ⛩️" to "Official anime announcements, Japanese release dates, and OST downloads.",
                    "Netflix & Prime OTT Releases 📺" to "Weekly streaming calendar, Rotten Tomatoes scores, and trending TV shows.",
                    "Celebrity Red Carpet Official 💎" to "High-resolution red carpet glamour, fashion week highlights, and designer wear.",
                    "Hollywood Box Office Tracker 🎟️" to "Worldwide opening weekend figures, budget reports, and studio earnings.",
                    "Billboard Top 100 Charts 🏆" to "Global music charts, viral TikTok audio rankings, and gold record sales.",
                    "Gaming Trailers & Reveal Streams 🕹️" to "Official PlayStation, Xbox, and Nintendo game reveals and release dates.",
                    "Podcast Daily & Talk Shows 🎙️" to "Top audio podcasts, author interviews, and deep storytelling episodes.",
                    "Short Films & Indie Showcase 📽️" to "Curated short films, film festival winners, and director Q&A sessions.",
                    "Broadway & Performing Arts Wire 🎭" to "Stage theater productions, musical previews, and theatrical reviews.",
                    "Pop Culture & Viral Moments 🦄" to "Daily pop culture memes, trend breakdowns, and viral celebrity moments.",
                    "K-Pop Daily Idol Broadcast 💖" to "BTS, Blackpink, Twice comeback schedules, dance choreography, and teasers.",
                    "Vintage Bollywood Radio 📻" to "Remastered classics of Kishore Kumar, Lata Mangeshkar, and Rafi saab.",
                    "Soundtrack & Movie Scores Guild 🎻" to "Hans Zimmer, John Williams, and orchestral cinematic compositions.",
                    "Marvel & DC Comic Universe 🦸" to "MCU timeline leaks, superhero comic panels, and comic-con panels."
                )
            ),
            CategorySeedData(
                category = "Funny",
                groups = listOf(
                    "Daily Meme Factory & Dark Humor 💀" to "Top-tier dank memes, fresh Twitter screenshots, and original shitposts.",
                    "Desi Memes & Bakchodi Central 😜" to "Pure desi situational memes, college life banter, and viral family group jokes.",
                    "Funny Video Clips & Fails 📹" to "Instant laugh guaranteed! Slapstick humor, cute animal blunders, and epic fails.",
                    "Sarcasm & Savage Comebacks 🔥" to "Sharp witty remarks, roasting screenshots, sassy one-liners, and comeback guides.",
                    "Hostel & Backbencher Life 😂" to "Exam hall stress memes, warden pranks, mess food humor, and late night banter.",
                    "Cat & Dog Derpy Moments 🐱" to "Hilarious pets acting weird, goofy puppies, grumpy cats, and wholesome memes.",
                    "Office & Corporate Memes 💼" to "Monday blues, salary credit joy, boss micromanagement, and coffee survival humor.",
                    "Desi Wedding Fun & Bloopers 💃" to "Crazy baraat dances, confused uncle moments, and funny shaadi videos.",
                    "Bollywood Meme Templates & Clips 🎭" to "HD blank meme templates, Hera Pheri iconic dialogues, and reaction stickers.",
                    "Cricket & Sports Memes Gang 🏏" to "Troll memes on dropped catches, umpire blunders, and rival fan banter.",
                    "Husband Wife Jokes & Banter 👫" to "Classic married life humor, shopping jokes, cooking fails, and smile makers.",
                    "WhatsApp Forward Roasters 📱" to "Roasting fake forwarded remedies, ridiculous good morning gifs, and audio clips.",
                    "Engineering & Medical Student Tears 📚" to "Syllabus tears, viva room nightmares, professor trolls, and late submissions.",
                    "Late Night Gossip & Chuckles 🌙" to "Light-hearted midnight chats, funny confessions, and humorous life stories.",
                    "Gaming Rage & Noob Fails 🎮" to "Noob player mistakes, rage quits, hilarious voice chat moments, and memes.",
                    "Desi Standup Comedy Fans 😂" to "Sharing clips of Zakir Khan, Bassi, Tanmay Bhat, and emerging comedians."
                ),
                channels = listOf(
                    "Top 10 Memes Daily Official 🚀" to "The most viral 10 memes of the day voted by internet netizens.",
                    "Laugh Out Loud Shorts & Reels 😂" to "Handpicked high-definition comedy shorts, sketches, and pranks.",
                    "Sarcastic Tweets & One-Liners 🐦" to "The funniest thoughts, satire posts, and humorous observational quips.",
                    "Wholesome Comedy & Animal Antics 🐶" to "Feel-good laughs, smiling babies, clumsy pandas, and cheerful vibes.",
                    "Desi Humor Vault 🗄️" to "Evergreen classic comedic dialogue stickers, vintage comedy clips, and parodies.",
                    "Dad Jokes & Puns Unlimited 🧀" to "Groan-worthy puns, clever dad jokes, and funny riddle puzzles.",
                    "Viral TikTok & Insta Laughs 📱" to "Top comedy creators compilations, trending lip-syncs, and hilarious audios.",
                    "Corporate Life Sarcasm 👔" to "Humorous takes on Zoom calls, appraisal meetings, and corporate jargon.",
                    "Standup Comedy Highlights 🎤" to "Official tour announcement clips, punchline highlights, and backstage reels.",
                    "Unintentional Comedy & Fails 🤦" to "Funny signboards, hilarious translation errors, and comedic accidents.",
                    "College Life Chronicles 🎒" to "Canteen tales, exam panic, assignment drama, and friendship fun.",
                    "Gaming Bloopers & Glitches 👾" to "Physics engine breakdowns, funny game bugs, and hilarious clutch fails.",
                    "Cartoon & Animated Funnies 🎨" to "Looney Tunes clips, Tom & Jerry nostalgia, and witty animation strips.",
                    "Daily Chuckle Express ☕" to "Start your morning with 3 clean, hilarious jokes guaranteed to bring a smile.",
                    "Parody News & Satire Wire 📰" to "Fictional hilarious headlines, onion-style humor, and satire articles.",
                    "Epic Roasts & Savage Tweets 🎯" to "The sharpest burns, witty comeback replies, and roast threads on the web."
                )
            ),
            CategorySeedData(
                category = "Poetry",
                groups = listOf(
                    "Ghalib & Iqbal Urdu Shayari 📜" to "Deep philosophical ghazals, couplets, nazms, and interpretation of masters.",
                    "Jaun Elia Deewangi Club 🥀" to "Heartbreak, bitter truth, melancholy verses, and unmatched Jaun poetry fans.",
                    "Modern Urdu & Hindi Nazm Guild ✒️" to "Contemporary free verse, spoken poetry, and young budding poets' showcase.",
                    "Sufiana Kalam & Rumi Whispers 🕊️" to "Mystic poetry of Bulleh Shah, Baba Farid, Rumi, Shams Tabrizi, and Amir Khusro.",
                    "Dard-e-Dil & Sad Shayari 💔" to "Emotional couplets, lonely nights, bittersweet memories, and healing words.",
                    "Mushaira Live & Audio Bayts 🎤" to "Recordings of live mushairas, audio recitations, and tarannum performances.",
                    "English Classical Poetry Circle 📖" to "Keats, Wordsworth, Shakespearean sonnets, and Emily Dickinson reflections.",
                    "Two Line Shayari & Status Quotes ✍️" to "Crisp 2-liner poetic status lines for WhatsApp and Instagram stories.",
                    "Romantic Shayari & Ishq Lafz 💖" to "Sweet love poems, romantic couplets, confessing affection through words.",
                    "Faiz Ahmad Faiz Inqilabi Kalam ✊" to "Resistance poetry, social justice anthems, and humanist literature.",
                    "Tehzeeb Hafi & Zubair Ali Fans 🎙️" to "Latest couplets and video clips of young modern mushaira sensations.",
                    "Shayari Critique & Islah Forum 🖋️" to "Constructive meter (beher) checks, rhyme guidance, and vocabulary tips for poets.",
                    "Dosti & Yaari Poetic Stanzas 🤝" to "Verses celebrating lifelong friendships, loyalty, and school day bonds.",
                    "Mother Love & Walidain Nazm 🤱" to "Heart-touching poetry dedicated to mother's unconditional love and father's sacrifices.",
                    "Rain & Monsoon Shayari 🌧️" to "Petrichor, monsoon romanticism, raindrops on windows, and cup of tea poetry.",
                    "Khudi & Motivation Urdu Couplets 🦅" to "High-spirit couplets of Allama Iqbal and Ahmad Faraz for grit and self-realization."
                ),
                channels = listOf(
                    "Rekhta Classical Poetry Collection 📜" to "Verified authentic classical Urdu poetry, word meanings, and audio recitations.",
                    "Daily Urdu Sher of the Day 🌟" to "One exquisite Urdu couplet every morning with dictionary vocabulary breakdown.",
                    "Jaun Elia Exclusive Archives 🥀" to "Rare letters, unreleased verses, and soulful recitations of Jaun Elia.",
                    "Sufi Wisdom & Mystic Verses 🕊️" to "Spiritual illumination, divine love quotes, and classical sufi music excerpts.",
                    "Spoken Word Poetry & Voice Notes 🎙️" to "Studio recorded spoken word poetry with gentle piano ambient background.",
                    "Urdu Adab & Literary Masterpieces 📚" to "Essays on Manto, Ismat Chughtai, Patras Bokhari, and literary history.",
                    "Aesthetic Poetry Wallpapers 🖼️" to "Minimalist typography aesthetic wallpapers featuring timeless Urdu verses.",
                    "Ghazal Audio & Classical Mehfils 🎶" to "Mehdi Hassan, Jagjit Singh, and Farida Khanum high quality ghazal audio.",
                    "Faraz & Mohsin Naqvi Legacy 🖋️" to "Anthology of Ahmad Faraz and Mohsin Naqvi emotional masterpieces.",
                    "Haiku & Micro Poetry Corner 🍃" to "Beautiful minimalist three-line poems capturing nature and human emotions.",
                    "Women Poets of Urdu Literature 🌸" to "Parveen Shakir, Kishwar Naheed, and Ada Jafri timeless contributions.",
                    "World Poetry in Translation 🌐" to "Persian, Arabic, French, and Spanish poetry translated into Urdu & English.",
                    "Motivation & Resilience Verses ⚡" to "Uplifting poetry to conquer self-doubt, failure, and achieve greatness.",
                    "Nostalgic Childhood Nazms 🪁" to "Warm verses about paper boats, grandma stories, and sweet bygone childhood.",
                    "Night Owls & Moonlight Poetry 🌙" to "Reflective quiet verses for solitude, stars, midnight musings, and silence.",
                    "Voice of the Heart - Audio Shayari 🎧" to "Crisp HD voiceovers with soulful background violin and flute melodies."
                )
            ),
            CategorySeedData(
                category = "Videos",
                groups = listOf(
                    "Viral Reels & Status Clips 🎬" to "Trending short videos, 30-sec status clips, and HD vertical videos for WhatsApp.",
                    "Nature & 4K Drone Landscapes 🏔️" to "Relaxing drone footage of Swiss Alps, Northern valleys, waterfalls, and oceans.",
                    "Movie Scenes & Iconic Dialogues 🍿" to "High-octane cinema scenes, inspirational monologues, and climax action cuts.",
                    "Cinematic Video Editors Community 🖥️" to "CapCut templates, Premiere Pro transitions, and color grading LUTs.",
                    "Satisfying Oddly Loops & ASMR 🧼" to "Kinetic sand, soap cutting, woodworking ASMR, and visually soothing videos.",
                    "Wildlife & National Geographic Cuts 🦁" to "Fierce predator hunts, deep ocean creatures, and rare bird behaviors.",
                    "Automobile Drift & Supercar Revs 🏎️" to "Lamborghinis, GT-Rs, turbo sounds, track drag races, and tuning videos.",
                    "Cooking & Street Food Shorts 🍜" to "Mouth-watering fast food recipes, giant woks, and gourmet chef plating.",
                    "Magic Tricks & Mind Illusions 🪄" to "Street magicians, sleight of hand secrets revealed, and optical illusions.",
                    "Gym & Calisthenics Motivation 🏋️" to "Incredible human strength, bar skills, transformation timelines, and PRs.",
                    "Science Experiments & DIY Builds 🧪" to "Liquid nitrogen tests, magnetic levitation, resin casting, and DIY crafts.",
                    "Historical Documentaries & Archival Film 📽️" to "Colorized World War footage, ancient architecture, and lost civilizations.",
                    "Travel Vlogs & Backpacker Gems 🎒" to "Budget backpacking videos, secret travel spots, and street cultures worldwide.",
                    "Cute Puppies & Funny Kittens 🐾" to "Adorable animal clips, rescues, funny barking puppies, and kitten play.",
                    "Space & Galaxy Visualizations 🌌" to "Black hole simulations, James Webb telescope zoom-ins, and solar flares.",
                    "Dance, Hip-Hop & Choreography 💃" to "Breakdance battles, lyrical choreography, and viral dance routines."
                ),
                channels = listOf(
                    "Ultra HD 4K Scenic Clips 🌄" to "Breathtaking slow-motion landscapes and nature vistas formatted for screens.",
                    "Daily Status Video Vault 📱" to "Fresh daily 30-second video statuses curated for WhatsApp and social media.",
                    "Cinematography & Visual Arts Hub 🎥" to "Behind-the-scenes camera rigs, lighting setups, and indie film breakdowns.",
                    "Tech & Gadgets Hands-on Video 📱" to "Unboxing videos, teardowns, camera test shots, and durability drop tests.",
                    "Documentary Planet Official 🌍" to "Short-form documentaries covering history, culture, nature, and humanity.",
                    "Epic Space & Cosmic Journeys 🚀" to "Interstellar animations, planetary orbits, and NASA telescope observations.",
                    "Auto Speed & Motorsport Channel 🏁" to "Formula 1 overtakes, rally drifts, hypercar acceleration, and onboard laps.",
                    "Satisfying Art & Craft Time-Lapse 🎨" to "Pottery wheel time-lapse, oil painting speedpaints, and wood carving.",
                    "Street Food Stories Global 🌮" to "Authentic culinary short videos from Tokyo night markets to Istanbul alleys.",
                    "Extreme Sports & Adrenaline Rush 🏄" to "Wingsuit flying, big wave surfing, downhill mountain biking, and skydiving.",
                    "Mind-Bending Science Shorts 🔬" to "Microscope zoom-ins, chemical reactions, and quantum physics explained in 60s.",
                    "Animation & 3D Render Showcase 👾" to "Photorealistic CGI renders, motion graphics, and indie animation shorts.",
                    "Aviation Cockpit & Flight Vlogs ✈️" to "Boeing and Airbus cockpit landings in stormy weather and scenic approaches.",
                    "Historic Speeches & Turning Points 🎙️" to "Restored HD audio and video of speeches that shaped world history.",
                    "Gym Beast & Powerlifting Feats 🦾" to "World-record deadlifts, Olympic gymnastics, and athletic masteries.",
                    "Relaxing Ambient Music & Video 🌧️" to "Cozy rainy cafe visuals with jazz and rainfall for deep study and sleep."
                )
            ),
            CategorySeedData(
                category = "Education",
                groups = listOf(
                    "IELTS & Spoken English Club 🗣️" to "Daily vocabulary, IELTS band 8 speaking practice partners, and essay correction.",
                    "CSS & PMS Civil Services Aspirants 🏛️" to "Past papers, current affairs summaries, essay outlines, and interview prep.",
                    "Software Engineering & Python Coders 💻" to "Python, JavaScript, DSA problem solving, LeetCode solutions, and git help.",
                    "Medical & MBBS Study Group 🩺" to "Anatomy diagrams, pharmacology mnemonics, pathology cases, and USMLE guidance.",
                    "Physics & Advanced Mathematics Hub 📐" to "Calculus problem sets, quantum mechanics discussions, and theorem proofs.",
                    "Global Scholarships & Visa Guidance 🎓" to "Fully funded Erasmus Mundus, Fulbright, DAAD, and Chevening applications.",
                    "Graphic Design & UI/UX Guild 🎨" to "Figma resources, typography rules, portfolio reviews, and freelance gigs.",
                    "Accounting, Finance & ACCA Circle 📊" to "Audit standards, tax calculations, financial modeling, and ACCA exam tips.",
                    "History & World Civilizations 🏺" to "Ottoman Empire, Roman republic, Mughal history, and archaeological digs.",
                    "Business MBA & Case Study Forum 📈" to "Harvard business cases, market disruption frameworks, and startup models.",
                    "Data Science & AI/Machine Learning 🤖" to "TensorFlow, PyTorch, pandas, machine learning pipelines, and Kaggle comps.",
                    "Law & Judiciary Exam Prep ⚖️" to "Constitutional law, criminal penal codes, case precedents, and mock trials.",
                    "General Knowledge & Competitive Exams 🧠" to "Daily GK quizzes, world capitals, historical timelines, and trivia drills.",
                    "Arabic Language for Beginners 📖" to "Quranic Arabic grammar, conversational Gulf Arabic, and vocabulary cards.",
                    "German & French Language Exchange 🇩🇪" to "A1 to B2 level grammar, German vocabulary, pronunciation audio, and notes.",
                    "School Science & Chemistry Lab ⚗️" to "Organic chemistry mechanisms, periodic table trends, and high school notes."
                ),
                channels = listOf(
                    "Daily Oxford Vocabulary & Idioms 📚" to "Boost your English vocabulary with daily word origins, idioms, and usage.",
                    "Global Scholarships Alert Channel 🌍" to "Instant notifications for fully funded Master's, PhD, and undergrad grants.",
                    "Tech & AI Learning Digest 🤖" to "Curated tutorials on modern web development, prompt engineering, and cloud.",
                    "Current Affairs & World Map Factfile 🗺️" to "High-yield infomaps, geopolitical summaries, and economic indicators.",
                    "Medical Doctor Notes & Guidelines 🩺" to "Evidence-based clinical guidelines, ECG interpretations, and drug charts.",
                    "Civil Services Examination Notes 📝" to "Comprehensive summaries of national policies, budget papers, and debates.",
                    "Free Coding Books & Cheat Sheets 💻" to "Open-source programming references, cheat sheets, and algorithmic guides.",
                    "Psychology & Human Behavior 🧠" to "Cognitive biases, social psychology studies, and mental models for thinking.",
                    "Finance, Stocks & Investing 101 💰" to "Personal finance blueprints, compound interest, index funds, and taxes.",
                    "Space Science & Astronomy Channel 🔭" to "Astrophysics papers explained simply, exoplanet discoveries, and rockets.",
                    "World History in Maps & Photos 📜" to "Historic photographs, border evolutions, and forgotten ancient empires.",
                    "Figma & UI/UX Design System 🎨" to "Daily design systems tips, micro-interactions, and accessibility standards.",
                    "French & European Languages Hub 🇫🇷" to "Daily listening exercises, flashcards, and European university pathways.",
                    "Data Engineering & Cloud Architect ☁️" to "AWS, Azure, Docker, Kubernetes, and scalable system design blueprints.",
                    "Philosophy & Great Thinkers 🏛️" to "Stoicism, Socrates, Kant, and modern existential philosophy discussions.",
                    "Speed Math & Mental Calculation 🔢" to "Vedic math shortcuts, percentage tricks, and competitive exam math hacks."
                )
            ),
            CategorySeedData(
                category = "Sports",
                groups = listOf(
                    "Cricket Fans & Live Ball Commentary 🏏" to "Live score banter, playing XI discussions, PSL, IPL, and World Cup chatter.",
                    "Football Champions League & EPL ⚽" to "Real Madrid, Barca, Man Utd, Arsenal debate, match analysis, and tactics.",
                    "Fitness, Gym & Workout Motivation 🏋️" to "Diet macros, hypertrophy workout splits, supplement reviews, and form check.",
                    "Running, 10K & Marathon Runners 🏃" to "Pacing tips, running shoe reviews, recovery routines, and race schedules.",
                    "UFC, Boxing & Combat Sports 🥊" to "Pay-per-view fight predictions, MMA technique breakdowns, and weigh-ins.",
                    "Basketball NBA Fans & Highlights 🏀" to "Lakers, Warriors, trade rumors, MVP race debates, and fantasy league.",
                    "Tennis Grand Slam & ATP Tour 🎾" to "Wimbledon, Australian Open, Roland Garros, racket specs, and player news.",
                    "Cycling & Mountain Bike Trail Riders 🚴" to "Trail routes, road bike maintenance, Strava clubs, and endurance tips.",
                    "Badminton Smashers Club 🏸" to "Smash techniques, Yonex racket reviews, court bookings, and local tournaments.",
                    "Swimming & Aquatics Community 🏊" to "Stroke biomechanics, breathing endurance, open water swims, and gear.",
                    "Formula 1 Paddock & Telemetry 🏎️" to "Aerodynamic upgrades, tire strategies, race weekend predictions, and drama.",
                    "Table Tennis & Ping Pong Club 🏓" to "Spin rubbers, serve variations, footwork drills, and friendly weekend games.",
                    "Chess Grandmasters & Tactics ♟️" to "Chess puzzles, opening repertoire, endgames, and online blitz matches.",
                    "Hiking & Mountaineering Expeditions 🧗" to "K2, Everest base camp, trekking gear checklists, and mountain weather.",
                    "Billiards & Snooker Masters 🎱" to "Break building, cue ball positioning, Ronnie O'Sullivan shots, and tips.",
                    "Volleyball & Beach Sports League 🏐" to "Spiking drills, setter positioning, beach volleyball tourneys, and matches."
                ),
                channels = listOf(
                    "Live Cricket Scores & Ball-by-Ball 🏏" to "Instant ball-by-ball updates, wagon wheels, and official match highlights.",
                    "Football Transfer News - Fabrizio Style ⚽" to "Verified transfer deals, contract signings, medicals, and 'Here We Go' alerts.",
                    "Formula 1 Grid & Telemetry Live 🏎️" to "Live lap times, pitstop telemetry, steward investigations, and race reports.",
                    "UFC & Boxing Championship Wire 🥊" to "Fight card results, knockout videos, official scorecards, and press notes.",
                    "Gym Workout Plans & Nutrition PDF 🥗" to "Free downloadable meal plans, training splits, and evidence-based fitness.",
                    "NBA Highlights & Buzzer Beaters 🏀" to "Top 10 dunks of the night, clutch shots, and game recap video streams.",
                    "Tennis World Tour Updates 🎾" to "ATP and WTA live rankings, match point videos, and tournament schedules.",
                    "Marathon & Endurance Athletics 🏅" to "Training schedules, hydration science, and Olympic athletic updates.",
                    "World Chess News & Puzzles ♟️" to "Daily tactical puzzle of the day, grandmaster game reviews, and FIDE ratings.",
                    "Winter Sports & Extreme Snow 🏂" to "Downhill skiing, snowboarding championships, and winter Olympic coverage.",
                    "Motorsport & MotoGP Championship 🏍️" to "MotoGP pole positions, rider injuries, lean angle telemetry, and podiums.",
                    "Golf PGA Tour & Majors Digest ⛳" to "Leaderboards, eagle shots, Masters tournament updates, and swing tips.",
                    "Kabaddi & Traditional Sports 🤼" to "Pro Kabaddi highlights, raid points, wrestling bouts, and regional events.",
                    "Sports Medicine & Injury Recovery 🩹" to "ACL rehab, hamstring strains, physical therapy tips, and athlete longevity.",
                    "Youth Sports Academy & Scouting 🌟" to "Grassroots talent identification, college athlete scouts, and drills.",
                    "Olympics & World Championship Digest 🥇" to "Medal tallies, world record attempts, and multi-sport event coverage."
                )
            ),
            CategorySeedData(
                category = "Science",
                groups = listOf(
                    "Astrophysics & Space Discoveries 🌌" to "James Webb space telescope findings, black holes, dark matter, and cosmology.",
                    "Artificial Intelligence & Future Tech 🤖" to "LLMs, neural networks, quantum computing, robotics, and singularity talks.",
                    "Biotechnology & Genetics Frontier 🧬" to "CRISPR gene editing, synthetic biology, stem cell trials, and longevity.",
                    "Renewable Energy & Green Tech ☀️" to "Next-gen solar cells, fusion reactors, hydrogen power, and battery storage.",
                    "Theoretical Physics & String Theory ⚛️" to "General relativity, particle accelerators, CERN experiments, and math models.",
                    "Neuroscience & Mind Brain Interface 🧠" to "Neuralink, neuroplasticity, memory formation, and consciousness studies.",
                    "Geology & Volcanic Seismology 🌋" to "Earthquake tracking, plate tectonics, mineralogy, and volcanic alerts.",
                    "Nanotechnology & Advanced Materials 🔬" to "Graphene, carbon nanotubes, metamaterials, and superconductor claims.",
                    "Oceanography & Deep Sea Marine Life 🐋" to "Mariana trench exploration, bioluminescent animals, and coral ecosystems.",
                    "Microbiology & Viral Research 🧫" to "Bacterial resistance, immunology, microbiomes, and electron microscopy.",
                    "Aerospace Engineering & Rocketry 🚀" to "Orbital mechanics, ion thrusters, rocket engine cycles, and heat shields.",
                    "Paleontology & Dinosaur Fossils 🦖" to "Fossil excavations, prehistoric evolutionary trees, and amber specimens.",
                    "Meteorology & Severe Climate Dynamics 🌪️" to "Supercells, hurricanes, jet streams, and atmospheric modeling software.",
                    "Chemistry Experiments & Synthesis 🧪" to "Organic synthesis steps, crystal growing, catalysts, and laboratory tips.",
                    "Psychology & Behavioral Economics 💡" to "Cognitive heuristics, behavioral experiments, and evolutionary psychology.",
                    "Science Book Club & Paper Reviews 📚" to "Weekly reviews of breakthrough Nature and Science peer-reviewed papers."
                ),
                channels = listOf(
                    "NASA & ESA Space Mission Updates 🛰️" to "Direct mission dispatches from Mars rovers, lunar orbiters, and telescopes.",
                    "Daily Science Breakthroughs 🔬" to "Curated summaries of revolutionary scientific breakthroughs in under 2 minutes.",
                    "AI Innovations & Paper Summaries 🤖" to "Breakdown of the newest arXiv papers in machine learning and robotics.",
                    "Quantum World & Particle Physics ⚛️" to "Quantum computing progress, entanglement experiments, and subatomic physics.",
                    "Human Longevity & Biotech News 🧬" to "Anti-aging research, cellular rejuvenation, and clinical longevity trials.",
                    "Deep Space Astronomy Photo Stream 🔭" to "High-resolution astrophotography, nebulae, and distant spiral galaxies.",
                    "Futurism & Speculative Technology 🛸" to "Concepts of Dyson spheres, terraforming Mars, and advanced civilizations.",
                    "Earth Science & Climate Monitoring 🌍" to "Satellite remote sensing data, ice cap telemetry, and atmospheric sensors.",
                    "Brain & Cognitive Science Digest 🧠" to "Sleep science, neurochemistry, attention span studies, and memory hacks.",
                    "Materials That Will Change The Future 🪨" to "Self-healing concrete, aerogels, perovskites, and room-temp conductors.",
                    "Clean Energy Revolution Wire ⚡" to "Global transition to solar, offshore wind, and nuclear fusion experiments.",
                    "Marine Biology & Ocean Depths 🌊" to "Deep submergence discoveries and hydrothermal vent microbial ecology.",
                    "Microscopic Universe Revealed 🔬" to "Scanning electron microscope captures of everyday items and microorganisms.",
                    "History of Science & Inventors 📜" to "The stories behind Newton, Tesla, Einstein, Curie, and Turing's inventions.",
                    "Evolutionary Biology & Anthropology 🦴" to "Human ancestor fossil finds, genomic tracking of migrations, and evolution.",
                    "Do It Yourself Science Lab Guides ⚗️" to "Safe science experiments and electronics projects you can build at home."
                )
            ),
            CategorySeedData(
                category = "Friendship",
                groups = listOf(
                    "Global Friendship & Chill Lounge ☕" to "Make genuine friends worldwide, casual evening chats, and cultural sharing.",
                    "Late Night Chai & Deep Talks 🌙" to "No judgment zone for midnight thoughts, life advice, and heartwarming talks.",
                    "Introverts Quiet Hangout 🛋️" to "A calm, pressure-free space for introverts to share books, art, and comfort.",
                    "College Friends & Study Buddies 🎒" to "Find study partners, group study sessions, accountability, and chill breaks.",
                    "Travel Buddies & Roadtrippers 🚗" to "Find companions for weekend treks, road trips, camping, and city strolls.",
                    "Positive Vibes & Daily Gratitude 🌸" to "Share daily small wins, morning affirmations, and uplift one another.",
                    "Gamers Social Lobby & Co-op Friends 🎮" to "Find duo and squad partners for casual games, discord calls, and laughs.",
                    "Language Exchange & Cultural Friends 🗣️" to "Pair up with native speakers worldwide to practice languages and share life.",
                    "Coffee Lovers & Bookworms Corner 📖" to "Discuss novels, cozy cafe recommendations, favorite brews, and quotes.",
                    "Creative Artists & Sketch Buddies 🎨" to "Share sketches, digital art progress, motivate each other, and give feedback.",
                    "Movie Night & Virtual Watch Parties 🍿" to "Pick weekend movies, watch simultaneously, and share live commentary.",
                    "Music Jam & Playlist Swapping 🎧" to "Discover new indie artists, exchange Spotify playlists, and jam together.",
                    "Pet Owners & Animal Lovers Club 🐾" to "Show off your cats, dogs, parrots, and exchange pet care tips and photos.",
                    "Fitness Accountability Buddies 🏃" to "Keep each other consistent with daily workout check-ins and step counts.",
                    "Cooking Enthusiasts & Recipe Swappers 🍳" to "Share dinner pictures, family recipes, and cooking experiments.",
                    "Weekend Hangouts & City Explorers 🏙️" to "Meetup for coffee, museum tours, photowalks, and city festivals."
                ),
                channels = listOf(
                    "Daily Wholesome Moments & Stories 💖" to "Heartwarming true stories of kindness, friendship, and human compassion.",
                    "Thoughtful Quotes & Friendship Wisdom 🌿" to "Beautiful quotes on loyalty, companionship, and genuine connections.",
                    "Global Pen Pals & Cultural Exchange ✉️" to "Connect with international pen pals and experience different cultures.",
                    "Self-Care & Mental Well-Being Prompts 🧘" to "Gentle reminders to take a break, hydrate, and be gentle with yourself.",
                    "Curated Aesthetic Wallpapers & Vibes 🌻" to "Calming cozy aesthetic pictures, coffee aesthetics, and warm vibes.",
                    "Book Club Recommendations of the Week 📚" to "Curated book picks spanning fiction, self-growth, and memoirs.",
                    "Chill Lo-Fi Tracks & Study Beats 🎶" to "Weekly curated Lo-Fi chillhop playlists for studying, relaxing, and daydreaming.",
                    "Positive Affirmations for the Morning ☀️" to "Start your day with peaceful, grounded, and empowering thoughts.",
                    "Creative Writing Prompts & Journaling ✍️" to "Daily journaling ideas to reflect on life, relationships, and dreams.",
                    "Travel Inspiration & Scenic Getaways ✈️" to "Stunning hidden travel gems and roadtrip itineraries around the world.",
                    "Daily Dose of Cute Animals 🐱" to "A guaranteed smile: baby animals, fluffy kittens, and sweet rescue stories.",
                    "Mindful Living & Minimalism Tips 🍃" to "Decluttering your space, finding peace in simplicity, and mindful habits.",
                    "Art & Photography Weekly Showcase 📷" to "Inspiring visual art, cozy interiors, and street photography galleries.",
                    "Acts of Kindness Around the Globe 🤝" to "Inspiring news stories of ordinary people making the world better.",
                    "Evening Cozy Thoughts & Unwind 🍵" to "Soft evening reflections to help you let go of daily stress and sleep soundly.",
                    "Friendship Milestones & Memories 🎈" to "Celebrating true friendships, long distance bonds, and lifelong memories."
                )
            ),
            CategorySeedData(
                category = "Food",
                groups = listOf(
                    "Desi Biryani & Karahi Masters 🍲" to "Authentic Hyderabadi biryani, Shinwari karahi recipes, and secret spices.",
                    "Baking & Pastry Desserts Club 🧁" to "Fudgy brownies, sourdough starters, cheesecakes, and oven temperature tips.",
                    "Street Food Explorers & Reviews 🍢" to "Discover the best samosa spots, gol gappe, shawarmas, and night stalls.",
                    "Healthy Diet & Keto Meal Prep 🥗" to "Low-carb meal plans, calorie counting, keto fat bombs, and meal prepping.",
                    "Quick 15-Minute Home Dinners ⏱️" to "Effortless, delicious weeknight meals for busy students and professionals.",
                    "Vegetarian & Vegan Flavor Lab 🥑" to "Rich plant-based curries, paneer specials, lentils, and vegan desserts.",
                    "Barbecue, Grilling & Smoked Meats 🍖" to "Tikka boti marinades, seekh kebabs, brisket rubs, and charcoal grilling.",
                    "Italian Pasta & Woodfired Pizza 🍕" to "Handmade fettuccine, Neapolitan pizza dough fermenting, and sauces.",
                    "Seafood Lovers & Coastal Cuisine 🦐" to "Grilled pomfret, prawn curries, fish fry marinades, and coastal dining.",
                    "Tea, Chai & Karak Connoisseurs ☕" to "Doodh patti secrets, cardamom blends, Kashmiri chai, and snacks pairing.",
                    "Artisanal Coffee & Espresso Brewing ☕" to "V60 pour-overs, espresso machine dialing, latte art, and specialty beans.",
                    "Traditional Sweets & Mithai Making 🍮" to "Gulab jamun, barfi, gajar ka halwa, rasmalai, and festival treats.",
                    "Asian Noodles, Ramen & Dumplings 🍜" to "Rich bone broth ramen, spicy momos, pad thai noodles, and dim sum.",
                    "Smoothies, Detox Juices & Bowls 🍓" to "Nutrient-packed green juices, acai bowls, and natural fruit smoothies.",
                    "Budget Meals & Hostel Cooking 🍳" to "Cook delicious filling food with minimal utensils and budget ingredients.",
                    "Home Chefs & Cloud Kitchen Guild 👩‍🍳" to "Commercial recipe scaling, packaging, and home food business tips."
                ),
                channels = listOf(
                    "Daily Recipe Card & Video Guide 🍳" to "Step-by-step visual recipe cards with exact ingredient measurements.",
                    "Global Street Food Chronicles 🌮" to "The most famous street food stalls and vendors captured around the world.",
                    "Bakery Secrets & Cake Decorating 🎂" to "Professional frosting techniques, mirror glazes, and piping masterclasses.",
                    "Healthy Eating & Nutrition Facts 🥗" to "Evidence-based nutrition science, superfoods, and clean eating ideas.",
                    "Chai & Karak Lovers Broadcast ☕" to "Tea culture, regional chai recipes, and comforting tea-time accompaniments.",
                    "BBQ & Smoker Masterclass Wire 🥩" to "Meat temperature charts, dry rubs, barbecue wood types, and smoker tips.",
                    "Dessert Heaven & Sweet Tooth 🍫" to "Mouth-watering chocolate desserts, waffles, ice cream crafts, and tarts.",
                    "Quick Kitchen Hacks & Storage Tips 🔪" to "Keep veggies fresh longer, knife sharpening skills, and cooking shortcuts.",
                    "Traditional Heritage Cooking 🏺" to "Slow-cooked earthen pot handi, clay oven tandoor, and heirloom recipes.",
                    "Coffee Roasters & Barista Guide ☕" to "Bean origin profiles, roast levels, grinder settings, and brewing methods.",
                    "Vegan World & Plant Power 🌿" to "Vibrant nutrient-dense vegan recipes that taste rich and satisfying.",
                    "Asian Wok & Dim Sum Express 🥟" to "High-flame wok hei techniques, crispy egg rolls, and dumpling folds.",
                    "Gourmet Restaurant Reviews & Menus 🍽️" to "Michelin star insights, luxury restaurant reviews, and food aesthetics.",
                    "Seafood Market & Coastal Kitchen 🐟" to "Fish buying guides, freshness checks, and Mediterranean seafood recipes.",
                    "Party Appetizers & Finger Foods 🍢" to "Crowd-pleasing slider recipes, dip platters, cheese boards, and skewers.",
                    "Homemade Pickles, Sauces & Jams 🫙" to "Pickle fermentation, artisan chili sauces, garlic confit, and jams."
                )
            ),
            CategorySeedData(
                category = "Crypto",
                groups = listOf(
                    "Bitcoin & Ethereum Alpha Traders 📈" to "BTC macro cycles, ETH staking, on-chain whale alerts, and key support levels.",
                    "Crypto Day Trading & Futures Signals 🎯" to "Technical analysis, RSI divergence, stop loss risk management, and chart setups.",
                    "Altcoin Gems & Low-Cap Moonshots 🚀" to "Fundamental research on upcoming layer-1s, DePIN projects, and AI tokens.",
                    "DeFi Yield Farming & Staking Protocols 🌾" to "Liquidity pools, Uniswap v3 strategies, lending yields, and smart contract safety.",
                    "Solana Ecosystem & Fast Transactions ⚡" to "Solana memecoins, DEX volume spikes, Raydium launches, and wallet setups.",
                    "Crypto Airdrops & Testnet Hunters 🪂" to "Zero-cost testnet guides, bridging strategies, and verified airdrop trackers.",
                    "NFT Collectors & Web3 Gaming Guild 🎮" to "Digital art collections, metaverses, gaming tokens, and mint calendars.",
                    "Crypto Security & Hardware Wallets 🔐" to "Ledger/Trezor setups, avoiding phishing scams, and seed phrase best practices.",
                    "Mining, Hashes & Node Validators ⛏️" to "ASIC mining profitability, GPU rigs, running validator nodes, and power costs.",
                    "Crypto Tax, Legal & Regulation Hub ⚖️" to "Capital gains calculations, crypto tax filings, and global regulatory updates.",
                    "Memecoins & Degen Trading Room 🐸" to "High-risk high-reward trending meme tokens, liquidity locks, and radar alerts.",
                    "Macro Economics & Federal Reserve Pulse 🏛️" to "CPI reports, interest rates, dollar index (DXY), and impacts on crypto markets.",
                    "Algorithmic Trading & Python Bots 🤖" to "Binance API bots, grid trading strategies, and backtesting on TradingView.",
                    "Web3 Developers & Solidity Smart Contracts 💻" to "EVM development, Cairo, Rust smart contracts, and security audits.",
                    "Crypto Beginners Q&A & Onboarding 🐣" to "Friendly support for beginners learning how to buy, transfer, and store crypto.",
                    "Dollar Cost Averaging (DCA) Long Term HODL 🛡️" to "Stress-free long term wealth building using disciplined DCA into Bitcoin."
                ),
                channels = listOf(
                    "Bitcoin Daily Chart & On-Chain Metrics 📊" to "Daily glassnode on-chain charts, exchange reserves, and miner flows.",
                    "Crypto Breaking News & Whales Alert 🚨" to "Large exchange transfers, ETF inflows/outflows, and breaking regulations.",
                    "Verified Airdrop Step-by-Step Channel 🪂" to "Free step-by-step guides for claiming verified crypto project airdrops.",
                    "Futures Trading Setups & Key Levels 🎯" to "Daily technical analysis charts with entries, take profits, and stop losses.",
                    "Altcoin Research & Fundamental Reports 🔍" to "In-depth tokenomics, venture capital backing, and roadmaps of top coins.",
                    "DeFi & Yield Aggregators Wire 💰" to "Highest verified yields on stablecoins and audited decentralized protocols.",
                    "Solana & High-Speed Chain Radar ⚡" to "New ecosystem launches, developer grant winners, and volume surges.",
                    "Crypto Security Alerts & Scam Warnings ⚠️" to "Instant warnings about malicious smart contracts, drainers, and fake dApps.",
                    "Macro Crypto Outlook by Analysts 🌐" to "Global liquidity cycles, treasury yields, and Bitcoin 4-year halving trends.",
                    "Web3 & Blockchain Job Openings 💼" to "Remote developer, community manager, and marketing jobs in Web3.",
                    "NFT Floor Prices & Mint Calendar 🖼️" to "Blue-chip floor price tracker and upcoming curated digital art drops.",
                    "Token Unlocks & Vesting Schedule ⏳" to "Avoid dumps: calendar of upcoming token unlocks and investor vesting dates.",
                    "Crypto Regulations & Policy Tracker 📜" to "SEC filings, European MiCA guidelines, and central bank digital currency news.",
                    "Trading Psychology & Risk Discipline 🧠" to "Overcoming FOMO, managing drawdowns, and trading with stoic patience.",
                    "Crypto Educational Infographics 📈" to "Clear visual guides explaining blockchain technology, proof of stake, and keys.",
                    "Long-Term HODL & Halving Progress ⏳" to "Tracking Bitcoin halving cycle progress, stock-to-flow, and macro gains."
                )
            ),
            CategorySeedData(
                category = "Business",
                groups = listOf(
                    "Amazon FBA, Private Label & Wholesale 📦" to "Product hunting, supplier sourcing in China/Pakistan, PPC optimization, and scaling.",
                    "Shopify Dropshipping & E-Commerce 🛒" to "Winning product research, TikTok ad creatives, high-converting store funnels.",
                    "Digital Marketing & High-Ticket Clients 💰" to "Lead generation, Meta ads, cold email scripts, and closing $2k-$5k retainers.",
                    "Real Estate & Commercial Investments 🏢" to "Residential plots, rental yields, commercial plazas, and title deed due diligence.",
                    "Freelancers Guild: Upwork & Fiverr 💻" to "Profile optimization, proposal writing, rising talent badges, and client retention.",
                    "Startup Founders & Venture Pitching 🚀" to "Pitch decks, seed rounds, angel investors, business models, and cap tables.",
                    "Import Export & Customs Global Trade 🚢" to "Freight forwarding, container shipping, letters of credit, and customs duties.",
                    "Stock Market & Equity Investors Circle 📊" to "Fundamental company earnings, P/E ratios, dividend stocks, and index investing.",
                    "B2B Wholesale & Manufacturing Supply 🏭" to "Textile sourcing, agricultural commodities, packaging supplies, and factories.",
                    "Accounting, Taxation & Corporate Law 📑" to "Company incorporation, sales tax filings, audit compliance, and legal contracts.",
                    "Small Business Growth & Retail Shop 🏪" to "Inventory management, POS systems, local footfall, and retail customer service.",
                    "Affiliate Marketing & Passive Income 💸" to "SEO niche sites, affiliate networks, email newsletters, and content revenue.",
                    "YouTube Automation & Content Scaling 🎥" to "Channel monetization, scriptwriting teams, high CTR thumbnails, and RPMs.",
                    "HR, Hiring & Remote Team Management 👥" to "Recruiting top talent, payroll management, remote culture, and employee retention.",
                    "Graphic & Brand Identity Agency 🎨" to "Agency scaling, logo presentations, branding guidelines, and contract proposals.",
                    "Franchise Opportunities & Licensing 🤝" to "Food franchises, dealership rights, royalty structures, and ROI timelines."
                ),
                channels = listOf(
                    "Business Strategy & Case Studies 📈" to "How billion-dollar giants like Apple, Nike, and IKEA scaled their empires.",
                    "Daily E-Commerce Winning Products 🛍️" to "Curated viral product data, supplier links, competitor store analysis.",
                    "Startup Funding & Pitch Decks 💡" to "Real pitch decks that raised millions from Sequoia, Y Combinator, and a16z.",
                    "Real Estate Trends & Market Reports 🏙️" to "City property price trends, upcoming mega projects, and rental returns.",
                    "Amazon & E-Com Seller Bulletins 📦" to "Amazon policy updates, tariff shifts, holiday season logistics strategies.",
                    "Marketing Psychology & Ad Copy ✍️" to "Persuasive copywriting frameworks, psychological triggers, and ad breakdowns.",
                    "Global Trade, Shipping & Logistics 🚢" to "Container freight rates, shipping lane congestion, and export incentives.",
                    "Stock Market Daily Pre-Market Briefing 📊" to "Morning market outlook, corporate earnings calendar, and economic releases.",
                    "Freelance Gigs & High-Paying Leads 💼" to "Curated remote job leads, contract opportunities, and corporate gigs.",
                    "Tax Savings & Corporate Finance Tips 💰" to "Legal tax minimization strategies, business deductions, and wealth protection.",
                    "CEO Mindset & Executive Leadership 👔" to "Time management lessons, decision-making mental models, and leadership principles.",
                    "SaaS & Micro-App Businesses 💻" to "Building recurring revenue software, no-code tools, and acquiring subscribers.",
                    "Franchise & Dealership Directory 🏪" to "Verified franchise investment packages, capital requirements, and profitability.",
                    "Supply Chain & Manufacturing Direct 🏭" to "Direct factory listings, OEM partnerships, and product packaging innovations.",
                    "Negotiation & High-Stakes Sales Skills 🎯" to "Chris Voss negotiation tactics, objection handling, and closing masterclasses.",
                    "Personal Wealth & Investment Roadmap 🏦" to "Building diversified multi-asset wealth for long-term generational freedom."
                )
            )
        )

        val allItems = mutableListOf<ListingItem>()

        for ((catIndex, catData) in categoriesData.withIndex()) {
            val catName = catData.category

            // Add Groups (16 items)
            for ((groupIndex, pair) in catData.groups.withIndex()) {
                val (title, desc) = pair
                val isPromoted = groupIndex == 0 || groupIndex == 1 // Top 2 are featured
                val promoStart = if (isPromoted) now - (groupIndex * 1800000L) else null
                val promoEnd = if (isPromoted) now + threeDaysMs - (groupIndex * 3600000L) else null
                val linkSlug = title.filter { it.isLetterOrDigit() }.take(18).ifBlank { "Group${catIndex}_$groupIndex" }
                val fallbackImg = LinkMetadataFetcher.getDefaultImageForCategory(catName, ListingType.GROUP)

                allItems.add(
                    ListingItem(
                        id = "list_${catName.lowercase()}_grp_${groupIndex + 1}",
                        ownerId = "system_owner_${catName.lowercase()}_${groupIndex + 1}",
                        name = title,
                        description = desc,
                        imageUrl = fallbackImg,
                        whatsappLink = "https://chat.whatsapp.com/invite${linkSlug}Xy9",
                        category = catName,
                        type = ListingType.GROUP,
                        status = ListingStatus.APPROVED,
                        views = (2800 + (groupIndex * 950) + (catIndex * 320)).toLong(),
                        isPromoted = isPromoted,
                        promotionStart = promoStart ?: 0L,
                        promotionEnd = promoEnd ?: 0L,
                        createdAt = now - (groupIndex * 3600000L) - (catIndex * 7200000L)
                    )
                )
            }

            // Add Channels (16 items)
            for ((chanIndex, pair) in catData.channels.withIndex()) {
                val (title, desc) = pair
                val isPromoted = chanIndex == 0 || chanIndex == 1 // Top 2 are featured
                val promoStart = if (isPromoted) now - (chanIndex * 1500000L) else null
                val promoEnd = if (isPromoted) now + threeDaysMs - (chanIndex * 3600000L) else null
                val linkSlug = title.filter { it.isLetterOrDigit() }.take(20).ifBlank { "Channel${catIndex}_$chanIndex" }
                val fallbackImg = LinkMetadataFetcher.getDefaultImageForCategory(catName, ListingType.CHANNEL)

                allItems.add(
                    ListingItem(
                        id = "list_${catName.lowercase()}_chn_${chanIndex + 1}",
                        ownerId = "system_owner_${catName.lowercase()}_chn_${chanIndex + 1}",
                        name = title,
                        description = desc,
                        imageUrl = fallbackImg,
                        whatsappLink = "https://whatsapp.com/channel/0029Va${linkSlug}Zk7",
                        category = catName,
                        type = ListingType.CHANNEL,
                        status = ListingStatus.APPROVED,
                        views = (3500 + (chanIndex * 1250) + (catIndex * 410)).toLong(),
                        isPromoted = isPromoted,
                        promotionStart = promoStart ?: 0L,
                        promotionEnd = promoEnd ?: 0L,
                        createdAt = now - (chanIndex * 4200000L) - (catIndex * 6500000L)
                    )
                )
            }
        }

        return allItems
    }

    private data class CategorySeedData(
        val category: String,
        val groups: List<Pair<String, String>>,
        val channels: List<Pair<String, String>>
    )
}
