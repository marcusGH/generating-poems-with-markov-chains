import ndjson

books = {}
with open("gutenberg-poetry-v001.ndjson") as f:
    data = ndjson.load(f)
    for line in data:
        if not line['gid'] in books:
            books[line['gid']] = []
        books[line['gid']].append(line['s'])


print(books['20'])

# don't want these
# ignore = ['\n', '\t', '\xa0']
# def scrape_poem(lines):
#     # remove stuff to ignore
#     for i in range(len(lines)):
#         for ig in ignore:
#             lines[i] = lines[i].replace(ig, "")
#     # remove empty lines
#     lines = list(filter(lambda a: a != "", lines))
#
#     return lines

i = 0
numPoems = len(books)
for b in books.values():
    print(f"{i}/{numPoems} poems scraped")
    # make a file in a data directory
    with open(f"data/gutenberg/{i}", "w") as f:
        for line in b:
            f.write(line + " EOL ")
    i += 1
