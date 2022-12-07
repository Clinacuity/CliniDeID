var tag_description = {
'CC':'Coordinating conjunction',
'CD':'Cardinal number',
'DT':'Determiner',
'EX':'Existential there',
'FW':'Foreign word',
'IN':'Preposition or subordinating conjunction',
'JJ':'Adjective',
'JJR':'Adjective, comparative',
'JJS':'Adjective, superlative',
'LS':'List item marker',
'MD':'Modal',
'NN':'Noun, singular or mass',
'NNS':'Noun, plural',
'NNP':'Proper noun, singular',
'NNPS':'Proper noun, plural',
'PDT':'Predeterminer',
'POS':'Possessive ending',
'PRP':'Personal pronoun',
'PRP$':'Possessive pronoun (prolog version PRP-S)',
'RB':'Adverb',
'RBR':'Adverb, comparative',
'RBS':'Adverb, superlative',
'RP':'Particle',
'SYM':'Symbol',
'TO':'to',
'UH':'Interjection',
'VB':'Verb, base form',
'VBD':'Verb, past tense',
'VBG':'Verb, gerund or present participle',
'VBN':'Verb, past participle',
'VBP':'Verb, non-3rd person singular present',
'VBZ':'Verb, 3rd person singular present',
'WDT':'Wh-determiner',
'WP':'Wh-pronoun',
'WP$':'Possessive wh-pronoun (prolog version WP-S)',
'WRB':'Wh-adverb'
};

function tokenize(text) {
    text = text.replace(/^"/g, '`` ');
    text = text.replace(/([ \(\[{<])"/g, '$1 `` ');
    text = text.replace(/\.\.\./g, ' ... ');
    text = text.replace(/[,;:@#$%&]/g, ' $& ');
    text = text.replace(/([^.])([.])([\]\)}>"']*)[ ]*$/g, '$1 $2$3 ');
    text = text.replace(/[?!]/g, ' $& ');
    text = text.replace(/[][(){}<>]/g, ' $& ');
    text = text.replace(/--/g, ' -- ');
    text = text.replace(/$/g, ' ');
    text = text.replace(/^/g, ' ');
    text = text.replace(/"/g, " '' ");
    text = text.replace(/([^'])' /g, "$1 ' ");
    text = text.replace(/'([sSmMdD]) /g, " '$1 ");
    text = text.replace(/'ll /g, " 'll ");
    text = text.replace(/'re /g, " 're ");
    text = text.replace(/'ve /g, " 've ");
    text = text.replace(/n't /g, " n't ");
    text = text.replace(/'LL /g, " 'LL ");
    text = text.replace(/'RE /g, " 'RE ");
    text = text.replace(/'VE /g, " 'VE ");
    text = text.replace(/N'T /g, " N'T ");
    text = text.replace(/ ([Cc])annot /g, ' $1an not ');
    text = text.replace(/ ([Dd])'ye /g, " $1' ye ");
    text = text.replace(/ ([Gg])imme /g, ' $1im me ');
    text = text.replace(/ ([Gg])onna /g, ' $1on na ');
    text = text.replace(/ ([Gg])otta /g, ' $1ot ta ');
    text = text.replace(/ ([Ll])emme /g, ' $1em me ');
    text = text.replace(/ ([Mm])ore'n /g, " $1ore 'n ");
    text = text.replace(/ '([Tt])is /g, " '$1 is ");
    text = text.replace(/ '([Tt])was /g, " '$1 was ");
    text = text.replace(/ ([Ww])anna /g, ' $1an na ');
    text = text.replace(/\n/g, ' ');
    text = text.replace(/  */g, ' ');
    text = text.replace(/^ /g, '');
    text = text.replace(/ $/g, '');
    return text.split(' ');
}

function generate_features(words) {
    var output = [];
    for(var i = 0; i < words.length; i++) {
        var word = words[i];
        var containsNumber = 'N'; if(word.match(/\d/)) containsNumber = 'Y';
        var first = 'N'; if(i == 0) first = 'Y';
        var capitalized = 'N'; if(word.match(/^[A-Z][a-z]/)) capitalized = 'Y';
        var containsSymbol = 'N'; if(word.match(/[^\dA-Za-z]/)) containsSymbol = 'Y';
        var prefix1 = word.substr(0, 1); if(word.length < 1) prefix1 = '__nil__';
        var prefix2 = word.substr(0, 2); if(word.length < 2) prefix2 = '__nil__';
        var prefix3 = word.substr(0, 3); if(word.length < 3) prefix3 = '__nil__';
        var prefix4 = word.substr(0, 4); if(word.length < 4) prefix4 = '__nil__';
        var suffix1 = word.substr(word.length - 1, 1); if(word.length < 1) suffix1 = '__nil__';
        var suffix2 = word.substr(word.length - 2, 2); if(word.length < 2) suffix2 = '__nil__';
        var suffix3 = word.substr(word.length - 3, 3); if(word.length < 3) suffix3 = '__nil__';
        var suffix4 = word.substr(word.length - 4, 4); if(word.length < 4) suffix4 = '__nil__';
        output[i] = [word.toLowerCase(), containsNumber, first, capitalized, containsSymbol, prefix1, prefix2, prefix3, prefix4, suffix1, suffix2, suffix3, suffix4];
    }
    return output;
}

var TYPE_UNIGRAM = 1;
var TYPE_BIGRAM = 2;
//%U14:%x[-1,0]/%x[1,2]<trail>
function compile_template(text) {
    var output = { type:0, ids: [], text: [], trail: ""};
    if(text.match(/^U/)) output.type = TYPE_UNIGRAM;
    else if(text.match(/^B/)) output.type = TYPE_BIGRAM;
    else return null;
    var pattern = /(.*?)%x\[(-?\d+),(\d+)\]/g;
    var found;
    while(found = pattern.exec(text)) {
        output.ids.push([parseInt(found[2]), parseInt(found[3])]);
        output.text.push(found[1]);
    }
    found = text.match(/%x\[(-?\d+),(\d+)\]([^%]*?)$/);
    if(found) output.trail = found[3];
    else output.trail = text;
    return output;
}

function apply_templates(tokens) {
    var output = {unigram:[], bigram:[], tokens: tokens};
    for(var position = 0; position < tokens.length; position ++) {
        output.unigram.push([]);
        output.bigram.push([]);
        for(var i = 0; i < templates.length; i++) {
            if(typeof(templates[i]) == "string") templates[i] = compile_template(templates[i]);
            var template = templates[i];
            if(template == null) continue;
            var parts = [];
            for(var j = 0; j < template.ids.length; j++) {
                parts.push(template.text[j]);
                if(template.ids[j][0] + position >= 0 && template.ids[j][0] + position < tokens.length)
                    parts.push(tokens[template.ids[j][0] + position][template.ids[j][1]]);
                else
                    parts.push("_B" + template.ids[j][0]);
            }
            parts.push(template.trail);
            if(template.type == TYPE_UNIGRAM) {
                var key = parts.join("");
                if(key in weights) output.unigram[position].push(key);
            } else if(template.type == TYPE_BIGRAM) {
                var key = parts.join("");
                if(key in weights) output.bigram[position].push(key);
            }
        }
    }
    return output;
}

function unigramScore(cliques, position, tag) {
    var score = 0;
    var unigram = cliques.unigram[position];
    for(var i = 0; i < unigram.length; i++) {
        if(tag in weights[unigram[i]]) score += weights[unigram[i]][tag];
    }
    return score;
}

function bigramScore(cliques, position, previousTag, tag) {
    var score = 0;
    var bigram = cliques.bigram[position];
    for(var i = 0; i < bigram.length; i++) {
        var id = tag + previousTag * tags.length;
        if(id in weights[bigram[i]]) {
            score += weights[bigram[i]][id];
        }
    }
    return score;
}

function viterbi(cliques) {
    scores = [];
    backtrack = [];
    for(var position = 0; position < cliques.unigram.length; position++) {
        scores.push([]);
        backtrack.push([]);
        if(position == 0) {
            for(var i = 0; i < tags.length; i++) {
                scores[position][i] = unigramScore(cliques, position, i);
                backtrack[position][i] = -1;
            }
        } else {
            for(var i = 0; i < tags.length ; i++) {
                var max = 0;
                var argmax = null;
                var score_i = unigramScore(cliques, position, i);
                for(var j = 0 ; j < tags.length; j++) {
                    var score_j_i = score_i + scores[position - 1][j] + bigramScore(cliques, position, j, i);
                    if(argmax == null || score_j_i > max) {
                        max = score_j_i;
                        argmax = j;
                    }
                }
                scores[position][i] = max;
                backtrack[position][i] = argmax;
            }
        }
    }
    var max = 0;
    var argmax = null;
    for(var i = 0; i < tags.length; i++) {
        if(argmax == null || scores[scores.length - 1][i] > max) {
            max = scores[scores.length - 1][i];
            argmax = i;
        }
    }
    var output = {scores: scores, backtrack: backtrack, tags: []};
    var current = scores.length - 1;
    while(current >= 0) {
        output.tags.unshift(tags[argmax]);
        argmax = backtrack[current][argmax];
        current --;
    }
    return output;
}

function show_internals(words, features, cliques, prediction) {
    var target = document.getElementById("internals");
    if(target.style.display == 'none') return;
    var lines = [];
    lines.push("<ul><li><b>Features:</b></li></ul>");
    lines.push("<table>");
    for(var i = 0; i < words.length; i++) {
        lines.push("<tr><td>" + features[i].join("</td><td>") + "</td></tr>");
    }
    lines.push("</table>");
    lines.push("<ul><li><b>Cliques:</b></li></ul>");
    lines.push("<table>");
    for(var i = 0; i < words.length; i++) {
        lines.push("<tr><td>" + cliques.unigram[i].join("</td><td>") + "</td><td>" + cliques.bigram[i].join("</td><td>") +"</td></tr>");
    }
    lines.push("</table>");
    lines.push("<ul><li><b>Scores:</b></li></ul>");
    lines.push("<table>");
    lines.push("<tr><td></td><td>" + words.join("</td><td>") + "</td></tr>");
    for(var i = 0; i < tags.length; i++) {
        var line = [tags[i]];
        for(var j = 0; j < words.length; j++) {
            if(prediction.tags[j] == tags[i]) {
                line.push("<b>" + prediction.scores[j][i].toFixed(3) + "</b>");
            } else {
                line.push(prediction.scores[j][i].toFixed(3));
            }
        }
        lines.push("<tr><td>" + line.join("</td><td>") + "</td></tr>");
    }
    lines.push("</table>");
    target.innerHTML = "<p>" + lines.join("\n") + "</p>";
}

function run_tagger(text) {
    var words = tokenize(text);
    if(words[0] == "") return [];
    var features = generate_features(words);
    var cliques = apply_templates(features);
    var prediction = viterbi(cliques);
    var output = [];
    for(var i = 0; i < words.length; i++) {
        output.push(words[i] + '/<a name="" title="' + tag_description[prediction.tags[i]] + '">' + prediction.tags[i] + "</a>");
    }
    show_internals(words, features, cliques, prediction);
    return output;
}

function update() {
    var textarea = document.getElementById("input");
    var prediction = run_tagger(textarea.value, false);
    var result = document.getElementById("output");
    output.innerHTML = prediction.join(" ");
    gadgets.window.adjustHeight();
    return true;
}

function toggle_internals() {
    var internals = document.getElementById("internals");
    var more = document.getElementById("more");
    if(internals.style.display == "none") {
        internals.style.display = "block";
        more.innerHTML = "less...";
    } else {
        internals.style.display = "none";
        more.innerHTML = "more...";
    }
    update();
}
