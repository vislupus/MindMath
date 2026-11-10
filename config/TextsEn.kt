package com.vislupus.mindmath.config

import com.vislupus.mindmath.texts.AchievementText
import com.vislupus.mindmath.texts.MethodText
import com.vislupus.mindmath.texts.Texts

/**
 * =============================================================================
 *  ENGLISH TEXTS - every word the app shows in English.
 *
 *  Keep placeholders such as {0} and {1} exactly as they are: the app
 *  replaces them with numbers and names.
 * =============================================================================
 */
val TextsEn = Texts(
    languageCode = "en",
    languageName = "English",
    decimalSeparator = ".",

    // General
    cancel = "Cancel",
    close = "Close",
    next = "Next",
    done = "Done",

    // Bottom navigation
    navHome = "Home",
    navLearn = "Learn",
    navPractice = "Practice",
    navStats = "Stats",

    // Home
    greetingMorning = "Good morning",
    greetingAfternoon = "Good afternoon",
    greetingEvening = "Good evening",
    homeTagline = "A few minutes a day keeps your mind sharp.",
    dailyGoal = "Daily goal",
    goalProgress = "{0} of {1} problems",
    goalReached = "Goal reached",
    dailyWorkout = "Daily workout",
    dailyWorkoutDesc = "{0} mixed problems tuned to your level",
    startWorkout = "Start workout",
    streak = "Streak",
    dayOne = "day",
    dayMany = "days",
    level = "Level",
    levelNumber = "Level {0}",
    xpToNext = "{0} XP to level {1}",
    xpToday = "XP today",
    quickPractice = "Quick practice",
    speedRound = "Speed round",
    speedRoundDesc = "{0} seconds, as many as you can",
    mistakesReview = "Fix mistakes",
    mistakesReviewDesc = "{0} waiting for a second try",
    mistakesNone = "Nothing to fix right now",
    suggested = "Suggested for you",
    thisWeek = "This week",

    // Learn
    learnTitle = "Learn",
    learnSubtitle = "{0} techniques, each with a step-by-step demonstration",
    theIdea = "The idea",
    whenToUse = "When to use it",
    stepByStep = "Step by step",
    nextStep = "Next step",
    newExample = "New example",
    practiceThis = "Practice this method",
    tipTitle = "Tip",
    mastery = "Mastery",
    newBadge = "New",

    // Categories
    catAddSub = "Addition & subtraction",
    catMultiply = "Multiplication",
    catSquares = "Squares",
    catDivision = "Division",
    catPercent = "Percentages",

    // Difficulty
    difficulty = "Difficulty",
    levelAdaptive = "Adaptive",
    levelEasy = "Easy",
    levelMedium = "Medium",
    levelHard = "Hard",

    // Practice
    practiceTitle = "Practice",
    practiceSubtitle = "Pick a mode, or drill a single method.",
    modes = "Modes",
    methodDrill = "Method drill",
    methodDrillDesc = "Choose the difficulty and length, then tap a method.",
    problemsCount = "Problems",
    secondsValue = "{0} s",

    // Session
    check = "Check",
    skip = "Skip",
    hint = "Hint",
    correct = "Correct",
    wrong = "Not quite",
    correctAnswer = "Correct answer",
    yourAnswer = "Your answer",
    skipped = "Skipped",
    howToSolve = "How to solve it",
    quitTitle = "End this session?",
    quitMessage = "The answers you have given so far are saved.",
    quitConfirm = "End session",
    quitStay = "Keep going",
    inputDirection = "Typing direction",

    // Result
    resultPerfect = "Flawless!",
    resultGreat = "Great work",
    resultGood = "Good effort",
    resultKeepGoing = "Keep practising",
    resultCorrect = "{0} of {1} correct",
    accuracy = "Accuracy",
    avgTime = "Avg. time",
    xpEarned = "XP earned",
    newRecord = "New record",
    mistakesTitle = "Review your mistakes",
    practiceAgain = "Practice again",

    // Statistics
    statsTitle = "Statistics",
    statsSubtitle = "Your progress, day by day.",
    totalProblems = "Problems solved",
    timeTrained = "Time trained",
    bestStreak = "Best streak",
    activity = "Activity",
    metricProblems = "Problems",
    metricAccuracy = "Accuracy",
    metricTime = "Minutes",
    daysValue = "{0} days",
    calendar = "Calendar",
    less = "Less",
    more = "More",
    byMethod = "By method",
    answersValue = "{0} answers",
    noData = "No data yet",
    noDataDesc = "Solve a few problems and your progress will show up here.",
    achievements = "Achievements",
    bestSpeedRound = "Best speed round ({0} s)",

    // Settings
    settingsTitle = "Settings",
    language = "Language",
    theme = "Theme",
    themeSystem = "System",
    themeLight = "Light",
    themeDark = "Dark",
    dailyGoalDesc = "Problems per day",
    showTimer = "Show timer",
    showTimerDesc = "Display the stopwatch while you solve",
    showMethodName = "Show method name",
    showMethodNameDesc = "See which technique fits each problem",
    haptics = "Vibration",
    hapticsDesc = "Short feedback on keys and answers",
    rtlInput = "Type right to left",
    rtlInputDesc = "Enter the last digit first, as in ×11 and criss-cross",
    preferences = "Preferences",
    dataSection = "Data",
    storageInfo = "Everything is stored in a SQLite database inside the app's private folder. Nothing leaves this phone.",
    resetProgress = "Reset progress",
    resetDesc = "Delete all answers and statistics",
    resetTitle = "Reset all progress?",
    resetMessage = "Every answer, session and statistic will be deleted for good. Your settings are kept.",
    resetConfirm = "Reset",
    about = "About",
    version = "Version {0}",
    credits = "Icons by Lucide · Font: Roboto",

    // Units
    unitSeconds = "s",
    unitMinutes = "min",
    unitHours = "h",

    weekdaysShort = listOf("M", "T", "W", "T", "F", "S", "S"),

    words = mapOf(
        "of" to "of",
        "carry" to "carry",
        "rem" to "remainder",
    ),

    commonSteps = mapOf(
        "result" to "Answer",
        "halve" to "Halve it",
        "halve_again" to "Halve again",
        "double" to "Double it",
        "double_again" to "Double again",
        "round_up" to "See the second number as a round number minus a little",
        "split" to "Split the second number into parts",
    ),

    methods = mapOf(
        // ------------------------------------------------ Addition & subtraction
        "add_ltr" to MethodText(
            title = "Left-to-right addition",
            tagline = "Add the big parts first",
            idea = "On paper we add from right to left, but in your head it is easier to start with the biggest part. Keep the first number whole and add the second one piece by piece: thousands, hundreds, tens, ones. You say the answer in the order you compute it, and you always hold just one running total.",
            whenToUse = "Totals in a shop, adding up bills, scores, distances - any sum of two or more numbers.",
            tip = "Even if you stop halfway you already have a good estimate, because the biggest part comes first.",
            steps = mapOf(
                "add_thousands" to "Add the thousands",
                "add_hundreds" to "Add the hundreds",
                "add_tens" to "Add the tens",
                "add_ones" to "Add the ones",
            ),
        ),
        "add_round" to MethodText(
            title = "Round and adjust",
            tagline = "Add a round number, then fix it",
            idea = "Numbers ending in 7, 8 or 9 are awkward to add. Round them up to the next 10, 100 or 1000, add that easy number, then take back the little bit you added too much.",
            whenToUse = "Prices such as 3.99, 198 or 2 995, and any number that sits just below a round one.",
            tip = "Say the adjustment out loud first: \"198 is 200 minus 2\". Then the rest is automatic.",
            steps = mapOf(
                "add_rounded" to "Add the round number",
                "take_back" to "Take back the extra",
            ),
        ),
        "sub_round" to MethodText(
            title = "Subtract by rounding",
            tagline = "Take away too much, give some back",
            idea = "Subtracting 47 means borrowing in your head. Subtracting 50 does not. So take away the round number, then add back the difference, because you removed too much.",
            whenToUse = "Working out what is left after a payment, differences between prices, years and ages.",
            tip = "Rounding up means you subtracted too much, so the correction is always an addition.",
            steps = mapOf(
                "sub_rounded" to "Subtract the round number",
                "give_back" to "You took too much - add it back",
            ),
        ),
        "sub_complement" to MethodText(
            title = "Change from a round number",
            tagline = "All from 9, the last from 10",
            idea = "To subtract from 100, 1 000 or 10 000 there is no need to borrow: take every digit from 9 and only the last digit from 10. The digits you get, read in order, are the answer. If the round number is bigger (say 5 000), set 1 000 aside, use the rule on it and add the remaining 4 000 back.",
            whenToUse = "Counting change when you pay with a note, and \"how much is missing to...\" questions.",
            tip = "If the number ends in zeros, copy them and apply \"from 10\" to the last non-zero digit.",
            steps = mapOf(
                "comp_borrow" to "Set one round block aside",
                "comp_nines" to "Take every digit except the last from 9",
                "comp_last" to "Take the last digit from 10",
                "comp_join" to "Read the digits together",
                "comp_add_rest" to "Add the part you set aside",
            ),
        ),

        // ------------------------------------------------------- Multiplication
        "mul_11" to MethodText(
            title = "Multiply by 11",
            tagline = "Add the neighbours",
            idea = "Times 11 means times 10 plus the number itself, so every digit meets its neighbour. Work from right to left: the last digit stays, then each digit is added to the one on its right, and the first digit stays. When a sum goes over 9, carry 1 to the next step. For two digits it is simply: first digit, their sum, last digit.",
            whenToUse = "Eleven of anything, and as a stepping stone for ×22, ×33 or ×1.1 (adding 10%).",
            tip = "The digits appear from right to left. Turn on \"Type right to left\" in the session to enter them in that order.",
            steps = mapOf(
                "m11_last" to "The last digit stays as it is",
                "m11_pair" to "Add the two neighbours",
                "m11_pair_carry" to "Add the neighbours and carry",
                "m11_first" to "The first digit stays as it is",
                "m11_first_carry" to "First digit plus the carry",
            ),
        ),
        "mul_5_25" to MethodText(
            title = "Multiply by 5, 25, 50, 125",
            tagline = "Multiply by 10 or 100, then halve",
            idea = "5 is half of 10, 50 is half of 100, 25 is a quarter of 100 and 125 is an eighth of 1 000. So add zeros first, then halve: once for 5 and 50, twice for 25, three times for 125.",
            whenToUse = "Coins and notes, quantities packed by 5, 25 or 50, quarters of an hour.",
            tip = "If the number is even you can halve first and add the zeros afterwards: 48 × 5 → 24 → 240.",
            steps = mapOf(
                "m5_shift" to "Multiply by the round number instead",
            ),
        ),
        "mul_9_99" to MethodText(
            title = "Multiply by 9, 99, 999",
            tagline = "Times a round number, minus once",
            idea = "9 is 10 − 1, 99 is 100 − 1 and 999 is 1 000 − 1. Multiply by the round number by adding zeros, then subtract the original number once.",
            whenToUse = "Prices ending in 9 or 99, and anything that is \"one short\" of a round count.",
            tip = "The same works for 8 or 98 - just subtract the number twice.",
            steps = mapOf(
                "m9_round" to "See it as a round number minus 1",
                "m9_shift" to "Multiply by the round number",
                "m9_sub" to "Subtract the number once",
            ),
        ),
        "mul_double_halve" to MethodText(
            title = "Double and halve",
            tagline = "Trade one hard product for an easy one",
            idea = "A product does not change if you halve one factor and double the other. When one number is even and the other ends in 5, this swap turns the 5 into a 0 - and multiplying by a round number is easy.",
            whenToUse = "Even numbers times 15, 25, 35, 45...; areas and quantities such as 16 × 35.",
            tip = "Repeat the swap if it helps: 12 × 25 → 6 × 50 → 3 × 100.",
            steps = mapOf(
                "dh_step" to "Halve the first number, double the second",
                "dh_again" to "Do it once more",
                "dh_final" to "Now it is an easy product",
            ),
        ),
        "mul_ltr" to MethodText(
            title = "Multiply by one digit",
            tagline = "Left to right, part by part",
            idea = "Break the big number into hundreds, tens and ones. Multiply the biggest part first and keep a running total, adding each next product to it. There is nothing to carry and nothing to remember except one number.",
            whenToUse = "\"7 items at 347 each\", unit prices, converting weeks to days, and as the base of every bigger multiplication.",
            tip = "Say the running total out loud - it keeps your working memory free for the next product.",
            steps = mapOf(
                "ml_first" to "Start with the biggest part",
                "ml_next" to "Multiply the next part",
                "ml_add" to "Add it to the running total",
            ),
        ),
        "mul_split" to MethodText(
            title = "Split one number",
            tagline = "23 × 47 = 23 × 40 + 23 × 7",
            idea = "Split the second number into tens and ones. Multiply the first number by the tens, then by the ones, and add the two results. Each small product is a one-digit multiplication you already know how to do.",
            whenToUse = "Any two-digit times two-digit product: areas, totals, unit conversions.",
            tip = "Split the number with the smaller digits - it gives easier partial products.",
            steps = mapOf(
                "split_big" to "Multiply by the big part",
                "split_small" to "Multiply by the small part",
                "split_add" to "Add the two results",
            ),
        ),
        "mul_cross" to MethodText(
            title = "Criss-cross",
            tagline = "Multi-digit products, one digit at a time",
            idea = "Write the answer from right to left, one digit per step. First multiply the ones. Then cross-multiply (each digit with the opposite digit of the other number) and add the results. Finish with the leading digits. After each step keep the last digit and carry the rest. The same pattern extends to three digits and more: every step uses all digit pairs whose positions add up to the same column.",
            whenToUse = "Exact products of any two numbers when no shortcut applies. This is the general method for large multiplications.",
            tip = "Turn on \"Type right to left\" so you can enter each digit the moment you find it.",
            steps = mapOf(
                "cross_ones" to "Multiply the ones digits",
                "cross_middle" to "Cross-multiply and add (plus the carry)",
                "cross_last" to "Multiply the leading digits (plus the carry)",
            ),
        ),
        "mul_near_100" to MethodText(
            title = "Numbers near 100",
            tagline = "Use the distance from the base",
            idea = "For two numbers close to 100 (or 1 000), note how far each one is from the base. Add one number's distance to the other number and multiply by the base. Then add the product of the two distances. If one number is above the base and one below, the product of the distances is negative, so you subtract it.",
            whenToUse = "Products such as 97 × 94, 104 × 107 or 996 × 988 - they look hard but take seconds.",
            tip = "Both cross sums give the same result (97 − 6 = 94 − 3), which is a handy self-check.",
            steps = mapOf(
                "nb_diff" to "How far is each number from the base?",
                "nb_cross" to "Apply one distance to the other number",
                "nb_scale" to "Multiply by the base",
                "nb_product" to "Multiply the two distances",
                "nb_join" to "Combine the two results",
            ),
        ),
        "mul_diff_squares" to MethodText(
            title = "Around the middle",
            tagline = "47 × 53 = 50² − 3²",
            idea = "When two numbers are the same distance from a round number in between, their product is the square of the middle minus the square of the distance. One easy square, one tiny square, one subtraction.",
            whenToUse = "Pairs such as 19 × 21, 48 × 52 or 196 × 204.",
            tip = "Check whether the two numbers add up to twice a round number - if they do, this method applies.",
            steps = mapOf(
                "ds_center" to "Find the middle and the distance",
                "ds_big" to "Square the middle",
                "ds_small" to "Square the distance",
                "ds_sub" to "Subtract",
            ),
        ),

        // -------------------------------------------------------------- Squares
        "sq_5" to MethodText(
            title = "Squares ending in 5",
            tagline = "35² = 3 × 4, then 25",
            idea = "To square a number ending in 5, multiply the tens part by the next whole number and write 25 after it. The same trick works for any two numbers with the same tens whose ones add up to 10: multiply the tens by the next number, then write the product of the ones as two digits.",
            whenToUse = "Areas of squares, quick estimates, and products such as 43 × 47 or 62 × 68.",
            tip = "For products like 41 × 49 remember the leading zero: 4 × 5 = 20, then 09 → 2009.",
            steps = mapOf(
                "s5_head" to "Multiply the tens part by the next number",
                "s5_tail" to "Multiply the ones digits",
                "s5_join" to "Write the two results side by side",
            ),
        ),
        "sq_any" to MethodText(
            title = "Square any number",
            tagline = "Slide to a round number",
            idea = "Move the number to the nearest round number, and move a copy the same distance in the other direction. Multiply this pair (one of them is round, so it is easy) and add the square of the distance.",
            whenToUse = "Any square of a two-digit number, and three-digit numbers close to a hundred.",
            tip = "Learn the squares up to 25 by heart - they speed up many other methods.",
            steps = mapOf(
                "sq_slide" to "Go to the round number, and the same distance the other way",
                "sq_product" to "Multiply the pair",
                "sq_small" to "Square the distance",
                "sq_add" to "Add it",
            ),
        ),

        // ------------------------------------------------------------- Division
        "div_short" to MethodText(
            title = "Divide by one digit",
            tagline = "Left to right, carry the remainder",
            idea = "Start from the left. Divide the first digit (or the first two, if the first is too small) and say that digit of the answer. Put any remainder in front of the next digit and divide again. Repeat to the end.",
            whenToUse = "Splitting a bill, price per item, averages - the most common division of all.",
            tip = "Say each answer digit as soon as you have it, so you only need to remember the remainder.",
            steps = mapOf(
                "sd_first" to "Take the first digits that are big enough",
                "sd_next" to "Carry the remainder to the next digit",
            ),
        ),
        "div_5_25" to MethodText(
            title = "Divide by 5, 25, 50, 125",
            tagline = "Double, then move the decimal point",
            idea = "Dividing by 5 is the same as doubling and dividing by 10. For 50, double and divide by 100. For 25, double twice and divide by 100. For 125, double three times and divide by 1 000.",
            whenToUse = "Price per piece in packs of 5, 25 or 50; converting to quarters; averages over 5 days.",
            tip = "Doubling is far easier than dividing - whenever you see a 5, think \"double\".",
            steps = mapOf(
                "d5_shift" to "Move the decimal point",
            ),
        ),
        "div_factor" to MethodText(
            title = "Divide in two steps",
            tagline = "Split the divisor into factors",
            idea = "Dividing by 12 is hard, but dividing by 2 and then by 6 is easy. Break the divisor into two small factors and divide by them one after the other.",
            whenToUse = "Dozens, hours and minutes, splitting between 12, 15, 16, 24 or 36.",
            tip = "Divide by the factor that keeps the number whole and small first - usually 2, 4 or 5.",
            steps = mapOf(
                "df_split" to "Break the divisor into factors",
                "df_first" to "Divide by the first factor",
                "df_next" to "Then divide by the next one",
            ),
        ),
        "div_chunk" to MethodText(
            title = "Divide by two digits",
            tagline = "Take away big, easy chunks",
            idea = "Ask how many tens (or hundreds) of the divisor fit. Take that chunk away and see what is left. Then ask how many times the divisor fits in the remainder. The pieces of the answer add up to the result.",
            whenToUse = "Monthly cost from a yearly total, fuel consumption, cost per person in a larger group.",
            tip = "Round the divisor to estimate the first chunk: for 23, think \"about 20\".",
            steps = mapOf(
                "dc_chunk" to "Take the biggest easy multiple",
                "dc_left" to "What is left?",
                "dc_next" to "Now cover the rest",
                "dc_sum" to "Add up the pieces",
            ),
        ),

        // ---------------------------------------------------------- Percentages
        "pct_blocks" to MethodText(
            title = "Percent building blocks",
            tagline = "Build any percent from 10%, 5% and 1%",
            idea = "10% is the number with the decimal point moved one place left. 5% is half of that, and 1% is the point moved two places. Every percentage is a few of these blocks added together. 50% is a half and 25% is a quarter, which is even faster.",
            whenToUse = "Tips, VAT, discounts, interest, commissions - every everyday percentage.",
            tip = "For 90%, 95% or 99% it is quicker to subtract the missing piece from the whole.",
            steps = mapOf(
                "pb_10" to "Find 10% - move the decimal point",
                "pb_tens" to "Scale 10% up",
                "pb_5" to "5% is half of 10%",
                "pb_1" to "Find 1% - move the point two places",
                "pb_ones" to "Scale 1% up",
                "pb_sum" to "Add the pieces",
                "pb_half" to "50% is one half",
                "pb_quarter" to "25% is one quarter",
                "pb_quarter_of_half" to "25% is half of that",
                "pb_from_whole" to "Take it away from the whole",
            ),
        ),
        "pct_flip" to MethodText(
            title = "Flip the percentage",
            tagline = "8% of 50 = 50% of 8",
            idea = "x% of y is always equal to y% of x. When the percentage is awkward but the other number is friendly (50, 25, 20, 10...), swap them and the answer drops out immediately.",
            whenToUse = "Whenever the number after \"of\" would make a nicer percentage than the one in front.",
            tip = "Look for 50, 25, 20, 10 and 75 - they turn into a half, a quarter, a fifth, a tenth and three quarters.",
            steps = mapOf(
                "pf_flip" to "Swap the two numbers",
                "pf_half" to "50% is one half",
                "pf_quarter" to "25% is one quarter",
                "pf_fifth" to "20% is one fifth",
                "pf_tenth" to "10% is one tenth",
                "pf_three_quarters" to "75% is three quarters",
                "pf_times" to "More than 100%: multiply directly",
            ),
        ),
        "pct_change" to MethodText(
            title = "Discounts and mark-ups",
            tagline = "A price minus or plus a percentage",
            idea = "Find the percentage with the building blocks, then subtract it for a discount or add it for tax or a tip. With practice you can do it in one multiplication: 20% off is × 0.8, 20% more is × 1.2.",
            whenToUse = "Sale prices, VAT, tips, salary rises, price increases.",
            tip = "Minus 20% and then plus 20% does not bring you back - the second percentage is taken of a smaller number.",
            steps = mapOf(
                "pc_part" to "Find the percentage",
                "pc_sub" to "Take it off",
                "pc_add" to "Add it on",
                "pc_shortcut" to "Or in a single multiplication",
            ),
        ),
        "pct_of" to MethodText(
            title = "What percent is it?",
            tagline = "Turn a part into a percentage",
            idea = "Write the part over the whole as a fraction and simplify it. If the bottom can be scaled to 100, the top becomes the percentage. Otherwise divide top by bottom and move the decimal point two places.",
            whenToUse = "Test scores, progress towards a goal, shares of a budget, battery and discounts in reverse.",
            tip = "Know the classics by heart: 1/4 = 25%, 1/5 = 20%, 1/8 = 12.5%, 1/3 ≈ 33.3%.",
            steps = mapOf(
                "po_fraction" to "Write it as a fraction",
                "po_simplify" to "Write it as a fraction and simplify",
                "po_scale" to "Scale the bottom to 100",
                "po_divide" to "Divide, then move the point two places",
            ),
        ),
    ),

    achievementTexts = mapOf(
        "first_steps" to AchievementText("First steps", "Solve {0} problems"),
        "hundred" to AchievementText("Centurion", "Solve {0} problems"),
        "thousand" to AchievementText("Thousand club", "Solve {0} problems"),
        "week" to AchievementText("On fire", "Keep a {0}-day streak"),
        "month" to AchievementText("Unstoppable", "Keep a {0}-day streak"),
        "perfect" to AchievementText("Flawless", "Finish a session with no mistakes"),
        "speed" to AchievementText("Quick thinker", "Score {0} in a {1}-second speed round"),
        "explorer" to AchievementText("Explorer", "Try every method"),
        "scholar" to AchievementText("Scholar", "Open every lesson"),
        "master" to AchievementText("Master", "Fully master {0} methods"),
    ),
)
