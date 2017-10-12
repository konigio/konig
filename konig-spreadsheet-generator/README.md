
install:
```commandline
python3 -m pip install -r requirements.txt
```

usage:
```commandline
python3 ./app.py --help
usage: app.py [-h] [--format FORMAT] input output

Create spreadsheet from RDF/OWL

positional arguments:
  input            input OWL/RDF file
  output           output XLSX file

optional arguments:
  -h, --help       show this help message and exit
  --format FORMAT  RDF format

```

note: the output file will be saved as a XLSX file.