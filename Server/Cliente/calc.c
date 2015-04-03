 #include "soapH.h"

int main(int argc, char** argv)
{ struct soap *soap = soap_new();
  int a, b, result;
  if(argc > 3 )
  { a = atoi(argv[1]);
    b = atoi(argv[3]);
  }
  else
      return -1;
  switch (*argv[2]) {
  case '+':
    if(soap_call_ns__add(soap, "xmlcomponents.com/CalcBin/Calc.dll", NULL, a, b, &result) == 0)
      printf("%d+%d=%d\n", a, b, result);
    else
      soap_print_fault(soap, stderr);
    break;
  case '-':
    if(soap_call_ns__sub(soap, "xmlcomponents.com/CalcBin/Calc.dll", NULL, a, b, &result) == 0)
      printf("%d-%d=%d\n", a, b, result);
    else
      soap_print_fault(soap, stderr);
    break;
  }
  return 0;
}

// As always, a namespace mapping table is needed:
struct Namespace namespaces[] =
{   // {"ns-prefix", "ns-name"}
   {"SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/"},
   {"SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/"},
   {"xsi", "http://www.w3.org/2001/XMLSchema-instance"},
   {"xsd", "http://www.w3.org/2001/XMLSchema"},
   {"ns", "urn:Calc"}, // bind "ns" namespace prefix
   {NULL, NULL}
}; 


