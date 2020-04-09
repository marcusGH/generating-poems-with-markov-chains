data_directory = "data/selected-gutenberg-poems/"

lines = []

# we have 21 poems so far
for i in range(21):
    with open(data_directory + str(i), "r") as f:
        for l in f:
            # remove EOL endings
            s = l.replace('\n','')
            # remove numbers
            lines.append(''.join(i for i in s if not i.isdigit()))

# remove single lines bc. probably redundant stuff
for i in range(1, len(lines) - 1):
    if lines[i - 1] == '' and lines[i + 1] == '':
        lines[i] = ''

poems = []

# write a poem
p = ""

prevEmpty = False
for i in range(len(lines)):
    # double EOL, so probably end of poem
    if prevEmpty and lines[i] == '' and p != "":
        poems.append(p)
        p = ""
    # poem is continuing
    elif lines[i] != '':
        p += lines[i] + " EOL "

    if lines[i] == '':
        prevEmpty = True

# end last poem
if p != "":
    poems.append(p)
    
i = 0
for p in poems:
    # make a file in a data directory
    with open(f"data/formatted-poems/{i}", "w") as f:
        f.write(p)
    i += 1
