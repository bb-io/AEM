using Blackbird.Applications.Sdk.Common;
using Blackbird.Applications.Sdk.Common.Authentication.OAuth2;

namespace Apps.AEM
{
    public class AEMApplication : IApplication
    {
        public string Name
        {
            get => "AEM";
            set { }
        }

        public AEMApplication(){ }

        public T GetInstance<T>()
        {
            throw new NotImplementedException();
        }
    };
}


