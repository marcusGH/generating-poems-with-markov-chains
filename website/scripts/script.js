var bodyEl = document.querySelector("body")
var nextLineEl = bodyEl.querySelector("button#nextLine")
var alphaEl = bodyEl.querySelector("#alpha")
var textAreaEl = bodyEl.querySelector("#textarea")
var minLengthEl = bodyEl.querySelector("#minLineLength")
var maxLengthEl = bodyEl.querySelector("#maxLineLength")
var rhymeListLengthEl = bodyEl.querySelector("#rhymeListLength")
var numPathsEl = bodyEl.querySelector("#pathNum")
var rhymePatternEl = bodyEl.querySelector("#rhymePattern")

/* returns whether the word ends with a newline character */
function isEOL(text) {
	bool = text.endsWith("\n") || text.endsWith("\r") || text.endsWith("\r\n")
	return bool
}
 
// global variables
var alpha = -1
var minLength = -1
var maxLength = -1
var rhymeListLength = -1
var numPaths = -1

function pickFromDist(dist) {
	// find total probability mass
	var probMass = 0.0
	for (var i = 0; i < dist.length; i++) {
		probMass += dist[i][1]
	}

	// random number in [0, probMass)
	var rand = (Math.random() * probMass)
	// select a random word
	var randWord = "NOWORDFOUND\n"
	var massAccum = 0
	for (var i = 0; i < dist.length; i++) {
		// it is the lucky chosen number :)
		if (rand > massAccum && rand < massAccum + dist[i][1]) {
			randWord = dist[i][0]
			break
		}
		massAccum += dist[i][1] } return randWord
}

function rhymesWith(token, rhymeList) {
	for (var i = 0; i < rhymeList.length; i++) {
		// the searched word rhymes
		if (token == rhymeList[i].word) {
			return true
		}
	}
	return false
}

function dfs(rhymeWord, rhymeDic, path, minLength, maxLength) {
	// figure out the keys
	var firstOrderKey = path.length == 0 ? rhymeWord : path[path.length - 1]
	var secondOrderKey = path.length >= 2 ? path[path.length - 2] + " " +
		path[path.length - 1] : false

	var randNum = Math.random()
	var nextWord = "NOWORDFOUND"

	// do second order if:
	// - sufficient words for second order
	// - second order key exists
	// - random number says so as well
	if (secondOrderKey && secondOrderKey in secondOrder && randNum < alpha) {
		var dist = secondOrder[secondOrderKey]
		nextWord = pickFromDist(dist)
	}
	// otherwise do first order
	else {
		var dist = []
		if (firstOrderKey in firstOrder)
			dist = firstOrder[firstOrderKey]
		else if (firstOrderKey.replace(/\r?\n|\r|\n/g, '') in firstOrder)
			dist = firstOrder[firstOrderKey.replace(/\r?\n|\r|\n/g, '')]
		else if (firstOrderKey + '\n' in firstOrder)
			dist = firstOrder[firstOrderKey + '\n']
		else {
			console.error("SHIT! This is NOT supposed to happen!")
			dist = firstOrder[""]
		}
		nextWord = pickFromDist(dist)
	}

	// add the word
	nextPath = Object.assign([], path)
	nextPath.push(nextWord)

	// return the path if all criteria are fulfilled
	if (path.length >= minLength && path.length <= maxLength &&
		(rhymeWord == "" || rhymesWith(nextWord, rhymeDic))) {
		return nextPath
	}
	// otherwise if we can still remain in bounds, keep searching
	else if (path.length < maxLength) {
		return dfs(rhymeWord, rhymeDic, nextPath, minLength, maxLength)
	}
	// otherwise abort
	else
		return []
}

// keep track of what we've rhymed so far
var needOddRhyme = false
var needEvenRhyme = false
var needPairRhyme = false

// gets the last couple of lines as arrays
function getNextLine(penLastLine, lastLine, pairRhyme, evenLine) {
	// figure out the past two words
	var prevPrevWord = ""
	var prevWord = ""
	var rhymeWord = ""
	temp = penLastLine.concat(lastLine)
	if (temp.length > 1)
		prevPrevWord = temp[temp.length - 2]
	if (temp.length > 0)
		prevWord = temp[temp.length - 1]
	
	// use pair rhymes to figure out rhyme word
	if (pairRhyme && lastLine.length > 0)
		rhymeWord = lastLine[lastLine.length - 1]
	// or cross rhymes
	if (!pairRhyme && penLastLine.length > 0)
		rhymeWord = penLastLine[penLastLine.length - 1]

	// get all the rhyme words
	var rhymeListTemp = rhymes(rhymeWord, rhymeListLength)

	// remove stuff of same word eg. "and" rhymes with "and"
	for (var i = 0; i < rhymeListTemp; i++) {
		if (rhymeListTemp[i].word == rhymeWord)
			rhymeListTemp[i].word = "IGNOREIGNOREIGNORE"
	}

	// find path to next rhyming word -----------------
	
	// we just did a pair rhyme
	if (pairRhyme && !needPairRhyme) {
		rhymeWord = ""
	}
	// we just did a cross rhyme 
	if (!pairRhyme && (evenLine && !needEvenRhyme || !evenLine && !needOddRhyme))
		rhymeWord = ""

	var nextLineList = []
	for (var i = 0; i < numPaths; i++) {
		var res = dfs(rhymeWord, rhymeListTemp, [], minLength, maxLength)
		// store result
		if (res.length > 0) {
			nextLineList = res
			// the next should be a rhyming word
			if (pairRhyme)
				needPairRhyme = !needPairRhyme
			// we did an even cross rhyme
			if (!pairRhyme && evenLine)
				needEvenRhyme = !needEvenRhyme
			// we just did an odd cross rhyme
			if (!pairRhyme && !evenLine)
				needOddRhyme = !needOddRhyme
			// debug
			console.log("Found a word rhyming with " + 
				rhymeWord + ": " + nextLineList[nextLineList.length - 1])
			break
		}
	}
	// didn't find anything so just ignore rhyming
	if (nextLineList.length == 0) {
		console.log("didn't find any words rhyming with " + rhymeWord)
		nextLineList = dfs("", rhymeListTemp, [], minLength, maxLength)
		// reset rhyme counters
		if (evenLine)
			needEvenRhyme = true
		else
			needOddRhyme = true
		needPairRhyme = true
	}
	// console.log(needPairRhyme)

	var line = ""

	// clean up EOLs
	for (var i = 0; i < nextLineList.length; i++) {
		if (i == 0)
			line += nextLineList[i].replace(/\r?\n|\r|\n/g, '');
		else
			// remove EOL
			line += " " + nextLineList[i].replace(/\r?\n|\r|\n/g, '');
	}

	return line + "\n"
}

nextLineEl.addEventListener("click", function() {
	alpha = Number(alphaEl.value)
	minLength = Number(minLengthEl.value)
	maxLength = Number(maxLengthEl.value)
	rhymeListLength = Number(rhymeListLengthEl.value)
	numPaths = Number(numPathsEl.value)
	var pair = rhymePatternEl.value == "pair"
	

	var text = textAreaEl.value
	
	// there is an empty string as its own line at the end
	var lines = text.split(/\r?\n/)
	var penLastLine = []
	var lastLine = []

	if (lines.length > 2)
		penLastLine = lines[lines.length - 3].split(" ")
	if (lines.length > 1)
		lastLine = lines[lines.length - 2].split(" ")
	
	var evenLine = lines.length % 2 != 0

	var nextLine = getNextLine(penLastLine, lastLine, pair, evenLine)
	textAreaEl.value = text + nextLine
})
