//Contents of file "calc.cpp":
#include "soapH.h"
int main()
{
   return soap_serve(soap_new()); // use the service operation request dispatcher
}
// Implementation of the "add" service operation:
int ns__add(struct soap *soap, int a, int b, int *result)
{
   *result = a + b;
   return SOAP_OK;
}
// Implementation of the "sub" service operation:
int ns__sub(struct soap *soap, int a, int b, int *result)
{
   *result = a - b;
   return SOAP_OK;
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
